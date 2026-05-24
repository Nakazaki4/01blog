import {
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  OnInit,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../auth/auth.service';
import { PostSnippetService } from '../../components/post-snippet/post-snippet.service';
import { Post, PostResponse } from '../../components/post-snippet/post-snippet';
import { PostHost } from '../../components/post-snippet/post-host';
import { PostEventsService } from '../../shared/post-events.service';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-home',
  imports: [Post],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent extends PostHost implements OnInit {
  private auth = inject(AuthService);
  private postService = inject(PostSnippetService);
  private postEvents = inject(PostEventsService);
  private destroyRef = inject(DestroyRef);

  user = this.auth.currentUser;
  isAuthenticated = computed(() => !!this.user());
  currentUserId = computed(() => {
    const raw = this.user()?.userId;
    return raw != null ? Number(raw) : null;
  });

  posts = signal<PostResponse[]>([]);
  loading = signal(false);
  loadingMore = signal(false);
  error = signal<string | null>(null);
  hasMore = signal(true);

  private page = 0;
  private sentinel = viewChild<ElementRef<HTMLElement>>('sentinel');

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
  }

  ngOnInit(): void {
    this.loadFeed();
    this.postEvents.created$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((post) => this.posts.update((current) => [post, ...current]));
  }

  loadFeed(): void {
    this.loading.set(true);
    this.error.set(null);
    this.page = 0;
    this.hasMore.set(true);
    this.postService.getFeed(0, PAGE_SIZE).subscribe({
      next: (page) => {
        this.posts.set(page.content);
        this.hasMore.set(page.number + 1 < page.totalPages);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to load feed');
        this.loading.set(false);
      },
    });
  }

  private loadMore(): void {
    if (this.loading() || this.loadingMore() || !this.hasMore()) return;
    this.loadingMore.set(true);
    const nextPage = this.page + 1;
    this.postService.getFeed(nextPage, PAGE_SIZE).subscribe({
      next: (page) => {
        this.posts.update((current) => [...current, ...page.content]);
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
    this.posts.update((current) => current.filter((p) => p.id !== postId));
  }

  override onCommentCountChanged({ postId, commentCount }: { postId: number; commentCount: number }): void {
    this.posts.update((list) =>
      list.map((p) => (p.id === postId ? { ...p, commentCount } : p)),
    );
  }

  override onLikeToggled(postId: number): void {
    const current = this.posts().find(p => p.id === postId);
    if (!current) return;
    const nextLiked = !current.isLiked;

    this.posts.update(list => list.map(p =>
      p.id === postId ? {
        ...p,
        isLiked: nextLiked,
        likeCount: p.likeCount + (nextLiked ? 1 : -1)
      }
        : p
    ))

    const call = nextLiked ?
      this.postService.like(postId) :
      this.postService.unlike(postId)

    call.subscribe({
      error: () => {
        this.posts.update(list => list.map(p =>
          p.id === postId ? {
            ...p,
            isLiked: !nextLiked,
            likeCount: p.likeCount + (nextLiked ? -1 : 1)
          }
            : p
        ))
      }
    })
  }
}
