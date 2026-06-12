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
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../auth/auth.service';
import { PostResponse } from '../../components/post-snippet/post-snippet';
import {
  CommentResponse,
  PostSnippetService,
} from '../../components/post-snippet/post-snippet.service';
import { PostBodyComponent } from '../../components/post-body/post-body';

export interface PostDetailData {
  post: PostResponse;
}

export interface PostDetailResult {
  postId: number;
  commentCount: number;
  isLiked: boolean;
  likeCount: number;
}

const MAX_COMMENT_LENGTH = 250;
const MIN_COMMENT_LENGTH = 12;
const PAGE_SIZE = 20;

@Component({
  selector: 'app-post-detail',
  imports: [
    DatePipe,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    PostBodyComponent,
  ],
  templateUrl: './post.html',
  styleUrl: './post.css',
})
export class PostDetailComponent {
  private auth = inject(AuthService);
  private dialogRef = inject(MatDialogRef<PostDetailComponent, PostDetailResult>);
  private destroyRef = inject(DestroyRef);
  private postService = inject(PostSnippetService);

  private page = 0;
  private sentinel = viewChild<ElementRef<HTMLElement>>('sentinel');

  post = inject<PostDetailData>(MAT_DIALOG_DATA).post;

  isAuthenticated = computed(() => !!this.auth.currentUser());

  commentContent = signal<string>('');
  comments = signal<CommentResponse[]>([]);
  commentCount = signal<number>(this.post.commentCount);
  isLiked = signal<boolean>(this.post.isLiked);
  likeCount = signal<number>(this.post.likeCount);
  likePending = signal<boolean>(false);
  loadingComments = signal<boolean>(false);
  loadingMore = signal<boolean>(false);
  hasMore = signal<boolean>(true);
  submitting = signal<boolean>(false);
  loadError = signal<string | null>(null);

  readonly maxLength = MAX_COMMENT_LENGTH;
  readonly minLength = MIN_COMMENT_LENGTH;

  canSubmit = computed(() => {
    const text = this.commentContent().trim();
    return (
      !this.submitting() &&
      text.length >= MIN_COMMENT_LENGTH &&
      text.length <= MAX_COMMENT_LENGTH
    );
  });

  constructor() {
    this.loadComments();

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

  loadComments(): void {
    this.loadingComments.set(true);
    this.loadError.set(null);
    this.page = 0;
    this.hasMore.set(true);
    this.postService.listComments(this.post.id, 0, PAGE_SIZE).subscribe({
      next: (page) => {
        this.comments.set(page);
        this.commentCount.set(page.length);
        this.hasMore.set(page.length + 1 < page.length);
        this.loadingComments.set(false);
      },
      error: (err) => {
        this.loadError.set(err.error?.message || 'Failed to load comments');
        this.loadingComments.set(false);
      },
    });
  }

  private loadMore(): void {
    if (this.loadingComments() || this.loadingMore() || !this.hasMore()) return;
    this.loadingMore.set(true);
    const nextPage = this.page + 1;
    this.postService.listComments(this.post.id, nextPage, PAGE_SIZE).subscribe({
      next: (page) => {
        this.comments.update((list) => [...list, ...page]);
        this.commentCount.set(page.length);
        this.page = page.length;
        this.hasMore.set(page.number + 1 < page.totalPages);
        this.loadingMore.set(false);
      },
      error: () => {
        this.loadingMore.set(false);
      },
    });
  }

  submitComment(): void {
    if (!this.canSubmit()) return;
    const content = this.commentContent().trim();
    this.submitting.set(true);
    this.postService.addComment(this.post.id, content).subscribe({
      next: (created) => {
        this.comments.update((list) => [created, ...list]);
        this.commentCount.update((n) => n + 1);
        this.commentContent.set('');
        this.submitting.set(false);
      },
      error: () => {
        this.submitting.set(false);
      },
    });
  }

  onLike(): void {
    if (!this.isAuthenticated() || this.likePending()) return;
    const nextLiked = !this.isLiked();
    this.isLiked.set(nextLiked);
    this.likeCount.update((n) => n + (nextLiked ? 1 : -1));
    this.likePending.set(true);
    const call = nextLiked
      ? this.postService.like(this.post.id)
      : this.postService.unlike(this.post.id);
    call.subscribe({
      next: () => this.likePending.set(false),
      error: () => {
        this.isLiked.set(!nextLiked);
        this.likeCount.update((n) => n + (nextLiked ? -1 : 1));
        this.likePending.set(false);
      },
    });
  }

  close(): void {
    this.dialogRef.close({
      postId: this.post.id,
      commentCount: this.commentCount(),
      isLiked: this.isLiked(),
      likeCount: this.likeCount(),
    });
  }
}