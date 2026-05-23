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
import { Post } from '../../components/post-snippet/post-snippet';
import { PostHost } from '../../components/post-snippet/post-host';
import { PostService } from '../../components/post-snippet/post-snippet.service';
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
  private postService = inject(PostService);
  private auth = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  profile = signal<UserProfile | null>(null);
  loading = signal(false);
  loadingMore = signal(false);
  error = signal<string | null>(null);
  hasMore = signal(false);

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

  override onPostDeleted(postId: number): void {
    const p = this.profile();
    if (!p) return;
    this.profile.set({ ...p, posts: p.posts.filter((post) => post.id !== postId) });
  }
}
