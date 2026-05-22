import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../auth/auth.service';
import { PostService } from '../../components/post-snippet/post-snippet.service';
import { Post, PostReport, PostResponse } from '../../components/post-snippet/post-snippet';
import { PostEventsService } from '../../shared/post-events.service';

@Component({
  selector: 'app-home',
  imports: [Post],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent implements OnInit {
  private auth = inject(AuthService);
  private postService = inject(PostService);
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
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadFeed();
    this.postEvents.created$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((post) => this.posts.update((current) => [post, ...current]));
  }

  loadFeed(): void {
    this.loading.set(true);
    this.error.set(null);
    this.postService.getFeed().subscribe({
      next: (page) => {
        this.posts.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to load feed');
        this.loading.set(false);
      },
    });
  }

  logout(): void {
    this.auth.logout();
  }

  onLikeToggled(postId: number): void {
    console.log('like toggled for post', postId);
  }

  onCommentClicked(postId: number): void {
    console.log('comment clicked for post', postId);
  }

  onPostEdited(postId: number): void {
    console.log('edit post', postId);
  }

  onPostDeleted(postId: number): void {
    console.log('delete post', postId);
  }

  onPostReported(report: PostReport): void {
    console.log('report submitted', report);
  }
}
