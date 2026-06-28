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
import { AdminService } from '../admin/admin.service';

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
  private adminService = inject(AdminService);
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
    this.posts.set([]);
    this.userService.getById(id).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.loading.set(false);
        this.loadInitialPosts(id);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to load profile');
        this.profile.set(null);
        this.loading.set(false);
      },
    });
  }

  private loadInitialPosts(id: string): void {
    this.postService.getByAuthor(id, 0, PAGE_SIZE).subscribe({
      next: (posts) => {
        this.posts.set(posts);
        this.hasMore.set(posts.length === PAGE_SIZE);
      },
      error: () => { },
    });
  }

  private loadMore(): void {
    const p = this.profile();
    if (!p || this.loading() || this.loadingMore() || !this.hasMore()) return;
    this.loadingMore.set(true);
    const nextPage = this.page + 1;
    this.postService.getByAuthor(p.id, nextPage, PAGE_SIZE).subscribe({
      next: (posts) => {
        this.posts.update((current) => [...current, ...posts]);
        this.page = nextPage;
        this.hasMore.set(posts.length === PAGE_SIZE);
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
    this.posts.update((list) => list.filter((p) => p.id !== postId));
  }

  override onPostEdited(postId: number): void {
    this.editPost(postId, this.posts);
  }

  override onPostHidden(postId: number): void {
    this.adminService.hidePost(postId).subscribe({
      next: () => {
        this.posts.update((list) => list.filter((p) => p.id !== postId));
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to hide post');
      },
    });
  }

  override onCommentCountChanged({ postId, commentCount }: { postId: number; commentCount: number }): void {
    this.posts.update((list) =>
      list.map((p) => (p.id === postId ? { ...p, commentCount } : p)),
    );
  }

  override onLikeStateChanged({ postId, isLiked, likeCount }: { postId: number; isLiked: boolean; likeCount: number }): void {
    this.posts.update((list) =>
      list.map((p) => (p.id === postId ? { ...p, isLiked, likeCount } : p)),
    );
  }

  override onLikeToggled(postId: number): void {
    if (this.likePendingIds().has(postId)) return;
    const current = this.posts().find((p) => p.id === postId);
    if (!current) return;
    const nextLiked = !current.isLiked;
    const nextLikeCount = current.likeCount + (nextLiked ? 1 : -1);

    this.updatePost(postId, { isLiked: nextLiked, likeCount: nextLikeCount });
    this.setLikePending(postId, true);

    const call = nextLiked
      ? this.postService.like(postId)
      : this.postService.unlike(postId);

    call.subscribe({
      next: () => this.setLikePending(postId, false),
      error: () => {
        this.updatePost(postId, { isLiked: current.isLiked, likeCount: current.likeCount });
        this.setLikePending(postId, false);
      },
    });
  }

  isLikePending(postId: number): boolean {
    return this.likePendingIds().has(postId);
  }

  private updatePost(postId: number, changes: Partial<PostResponse>): void {
    this.posts.update((list) =>
      list.map((p) => (p.id === postId ? { ...p, ...changes } : p)),
    );
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
