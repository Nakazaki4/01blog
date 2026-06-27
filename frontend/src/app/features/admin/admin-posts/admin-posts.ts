import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { stripMarkdown } from '../../../shared/markdown';
import { AdminPost, AdminService } from '../admin.service';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-admin-posts',
  imports: [DatePipe, RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './admin-posts.html',
  styleUrl: './admin-posts.css',
})
export class AdminPostsComponent implements OnInit {
  private admin = inject(AdminService);

  posts = signal<AdminPost[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  page = signal(0);
  totalElements = signal(0);
  actionPendingIds = signal<ReadonlySet<number>>(new Set());
  canLoadMore = signal(false);

  totalPages = computed(() => Math.max(1, Math.ceil(this.totalElements() / PAGE_SIZE)));

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts(page = this.page()): void {
    this.loading.set(true);
    this.error.set(null);
    this.admin.listPosts(page, PAGE_SIZE).subscribe({
      next: (result) => {
        this.canLoadMore.set(result.length < PAGE_SIZE ? false : true)  
        this.posts.set(result);
        this.totalElements.set(result.length);
        this.page.set(page);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to load posts');
        this.loading.set(false);
      },
    });
  }

  deletePost(post: AdminPost): void {
    if (this.isActionPending(post.id)) return;
    if (!confirm(`Delete post #${post.id}?`)) return;
    this.setActionPending(post.id, true);
    this.admin.deletePost(post.id).subscribe({
      next: () => {
        this.posts.update((posts) => posts.filter((item) => item.id !== post.id));
        this.totalElements.update((count) => Math.max(0, count - 1));
        this.setActionPending(post.id, false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to delete post');
        this.setActionPending(post.id, false);
      },
    });
  }

  toggleHide(post: AdminPost): void {
    if (this.isActionPending(post.id)) return;
    const willHide = !post.hidden;
    this.setActionPending(post.id, true);
    const request = willHide ? this.admin.hidePost(post.id) : this.admin.unhidePost(post.id);
    request.subscribe({
      next: () => {
        this.posts.update((posts) =>
          posts.map((item) => (item.id === post.id ? { ...item, hidden: willHide } : item)),
        );
        this.setActionPending(post.id, false);
      },
      error: (err) => {
        this.error.set(
          err.error?.message || (willHide ? 'Failed to hide post' : 'Failed to unhide post'),
        );
        this.setActionPending(post.id, false);
      },
    });
  }

  postSnippet(post: AdminPost): string {
    const text = stripMarkdown(post.description ?? '');
    return text.length > 180 ? `${text.slice(0, 180).trimEnd()}...` : text;
  }

  nextPage(): void {
    if (!this.canLoadMore()) return;
    this.loadPosts(this.page() + 1);
  }

  previousPage(): void {
    if (this.page() === 0) return;
    this.loadPosts(this.page() - 1);
  }

  isActionPending(id: number): boolean {
    return this.actionPendingIds().has(id);
  }

  private setActionPending(id: number, pending: boolean): void {
    this.actionPendingIds.update((ids) => {
      const next = new Set(ids);
      pending ? next.add(id) : next.delete(id);
      return next;
    });
  }
}
