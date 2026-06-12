import {
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { Post, PostResponse } from '../../components/post-snippet/post-snippet';
import { PostHost } from '../../components/post-snippet/post-host';
import { PostSnippetService } from '../../components/post-snippet/post-snippet.service';
import { UserProfile, UserService } from './user.service';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-user',
  imports: [Post],
  templateUrl: './user.html',
  styleUrl: './user.css',
})
export class UserComponent extends PostHost {
  private route = inject(ActivatedRoute);
  private userService = inject(UserService);
  private postService = inject(PostSnippetService);
  private auth = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  profile = signal<UserProfile | null>(null);
  posts = signal<PostResponse[] | []>([])
  likePendingIds = signal<ReadonlySet<number>>(new Set());
  loading = signal(false);
  loadingMore = signal(false);
  error = signal<string | null>(null);
  hasMore = signal(false);
  subscribePending = signal(false);

  private page = 0;
  private sentinel = viewChild<ElementRef<HTMLElement>>('sentinel');

  currentUserId = computed(() => {
    const raw = this.auth.currentUser()?.userId;
    return raw != null ? Number(raw) : null;
  });

  isSelf = computed(() => {
    const me = this.currentUserId();
    const id = this.profile()?.id;
    return me != null && id != null && me === id;
  });

  isAuthenticated = computed(() => this.auth.currentUser() != null);
  
  isAdmin = computed(() => this.auth.isAdmin());

  constructor() {
    super();

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((e) => e.isIntersecting)) {
          this.loadMore();
        }
      },
      { rootMargin: '400px' },
    );
    this.destroyRef.onDestroy(() => observer.disconnect());

    let observed: HTMLElement | null = null;
    effect(() => {
      const el = this.sentinel()?.nativeElement ?? null;
      if (el === observed) return;
      if (observed) observer.unobserve(observed);
      if (el) observer.observe(el);
      observed = el;
    });

    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (!id) {
        this.error.set('Missing user id');
        this.profile.set(null);
        return;
      }
      this.load(id);
    });
  }

  private load(id: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.page = 0;
    this.hasMore.set(false);
    this.userService.getById(id).subscribe({
      next: (p) => {
        this.profile.set(p);
        this.hasMore.set(p.posts.length === PAGE_SIZE);
        this.posts.set(p.posts)
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to load profile');
        this.profile.set(null);
        this.loading.set(false);
      },
    });
  }

  private loadMore(): void {
    const p = this.profile();
    if (!p || this.loading() || this.loadingMore() || !this.hasMore()) return;
    this.loadingMore.set(true);
    const nextPage = this.page + 1;
    this.postService.getByAuthor(p.id, nextPage, PAGE_SIZE).subscribe({
      next: (page) => {
        const current = this.profile();
        if (current) {
          this.profile.set({ ...current, posts: [...current.posts, ...page.content] });
          this.posts.set([...current.posts, ...page.content])
        }
        this.page = page.number;
        this.hasMore.set(page.number + 1 < page.totalPages);
        this.loadingMore.set(false);
      },
      error: () => {
        this.loadingMore.set(false);
      },
    });
  }

  logout(): void {
    this.auth.logout();
  }

  toggleSubscription(): void {
    const p = this.profile();
    if (!p || this.subscribePending()) return;
    const nextSubscribed = !p.isSubscribed;
    const delta = nextSubscribed ? 1 : -1;
    this.profile.set({
      ...p,
      isSubscribed: nextSubscribed,
      subscribersCount: p.subscribersCount + delta,
    });
    this.subscribePending.set(true);
    const call = nextSubscribed
      ? this.userService.subscribe(p.id)
      : this.userService.unsubscribe(p.id);
    call.subscribe({
      next: () => this.subscribePending.set(false),
      error: () => {
        const current = this.profile();
        if (current) {
          this.profile.set({
            ...current,
            isSubscribed: !nextSubscribed,
            subscribersCount: current.subscribersCount - delta,
          });
        }
        this.subscribePending.set(false);
      },
    });
  }

  override onPostDeleted(postId: number): void {
    const p = this.profile();
    if (!p) return;
    const posts = p.posts.filter((post) => post.id !== postId);
    this.profile.set({ ...p, posts });
    this.posts.set(posts);
  }

  override onCommentCountChanged({ postId, commentCount }: { postId: number; commentCount: number }): void {
    const p = this.profile();
    if (!p) return;
    const updated = p.posts.map((post) =>
      post.id === postId ? { ...post, commentCount } : post,
    );
    this.profile.set({ ...p, posts: updated });
    this.posts.set(updated);
  }

  override onLikeStateChanged({ postId, isLiked, likeCount }: { postId: number; isLiked: boolean; likeCount: number }): void {
    const p = this.profile();
    if (!p) return;
    const updated = p.posts.map((post) =>
      post.id === postId ? { ...post, isLiked, likeCount } : post,
    );
    this.profile.set({ ...p, posts: updated });
    this.posts.set(updated);
  }

  override onLikeToggled(postId: number): void {
    if (this.likePendingIds().has(postId)) return;
    const current = this.profile()?.posts.find(p => p.id === postId);
    if (!current) return;
    const nextLiked = !current.isLiked;
    const nextLikeCount = current.likeCount + (nextLiked ? 1 : -1);

    this.updatePost(postId, { isLiked: nextLiked, likeCount: nextLikeCount });
    this.setLikePending(postId, true);

    const call = nextLiked ?
      this.postService.like(postId) :
      this.postService.unlike(postId)

    call.subscribe({
      next: () => this.setLikePending(postId, false),
      error: () => {
        this.updatePost(postId, {
          isLiked: current.isLiked,
          likeCount: current.likeCount,
        });
        this.setLikePending(postId, false);
      }
    })
  }

  isLikePending(postId: number): boolean {
    return this.likePendingIds().has(postId);
  }

  private updatePost(postId: number, changes: Partial<PostResponse>): void {
    const current = this.profile();
    if (!current) return;
    const posts = current.posts.map((post) =>
      post.id === postId ? { ...post, ...changes } : post,
    );
    this.profile.set({ ...current, posts });
    this.posts.set(posts);
  }

  private setLikePending(postId: number, pending: boolean): void {
    this.likePendingIds.update((ids) => {
      const next = new Set(ids);
      if (pending) {
        next.add(postId);
      } else {
        next.delete(postId);
      }
      return next;
    });
  }
}
