import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule, MatMenuTrigger } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../features/auth/auth.service';
import { PostCreateDialogComponent } from '../../features/post-create/post-create-dialog';
import { PostResponse } from '../post-snippet/post-snippet';
import { PostEventsService } from '../../shared/post-events.service';
import {
  NotificationResponse,
  NotificationService,
  NotificationType,
} from '../../features/notification/notification.service';

@Component({
  selector: 'app-navbar',
  imports: [
    DatePipe,
    RouterLink,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class NavbarComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private postEvents = inject(PostEventsService);
  private notifications = inject(NotificationService);

  user = this.auth.currentUser;
  isAuthenticated = computed(() => !!this.user());
  isAdmin = computed(() => this.auth.isAdmin());

  unreadCount = this.notifications.unreadCount;
  items = signal<NotificationResponse[]>([]);
  loading = signal(false);
  loadError = signal<string | null>(null);

  redirectToDashboard(): void {
    this.router.navigate(['/admin']);
  }

  openCreatePost(): void {
    const ref = this.dialog.open<PostCreateDialogComponent, void, PostResponse>(
      PostCreateDialogComponent,
      { panelClass: 'post-create-panel', autoFocus: false },
    );
    ref.afterClosed().subscribe((post) => {
      if (post) this.postEvents.emitCreated(post);
    });
  }

  goToProfile(): void {
    const id = this.user()?.userId;
    if (id != null) this.router.navigate(['/profile', id]);
  }

  goToSettings(): void {
    this.router.navigate(['/settings']);
  }

  onBellOpened(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.notifications.list(0, 20).subscribe({
      next: (page) => {
        this.items.set(page);
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Failed to load notifications');
        this.loading.set(false);
      },
    });
  }

  onItemClick(item: NotificationResponse, trigger: MatMenuTrigger): void {
    if (!item.isRead) {
      this.items.update((list) =>
        list.map((n) => (n.id === item.id ? { ...n, isRead: true } : n)),
      );
      this.notifications.unreadCount.update((n) => Math.max(0, n - 1));
      this.notifications.markRead(item.id).subscribe({
        error: () => this.notifications.refreshUnreadCount(),
      });
    }
    trigger.closeMenu();
    this.navigateForItem(item);
  }

  markAllRead(): void {
    if (this.unreadCount() === 0) return;
    this.items.update((list) => list.map((n) => ({ ...n, isRead: true })));
    this.notifications.unreadCount.set(0);
    this.notifications.markAllRead().subscribe({
      error: () => this.notifications.refreshUnreadCount(),
    });
  }

  iconFor(type: NotificationType): string {
    switch (type) {
      case 'NEW_LIKE':
        return 'favorite';
      case 'NEW_COMMENT':
        return 'chat_bubble';
      case 'NEW_SUBSCRIBER':
        return 'person_add';
      case 'NEW_POST':
        return 'article';
    }
  }

  messageFor(item: NotificationResponse): string {
    switch (item.type) {
      case 'NEW_LIKE':
        return 'liked your post';
      case 'NEW_COMMENT':
        return 'commented on your post';
      case 'NEW_SUBSCRIBER':
        return 'subscribed to you';
      case 'NEW_POST':
        return 'published a new post';
    }
  }

  private navigateForItem(item: NotificationResponse): void {
    if (item.type === 'NEW_SUBSCRIBER') {
      this.router.navigate(['/profile', item.actor.id]);
    } else if (item.postId != null) {
      this.router.navigate(['/profile', item.actor.id]);
    }
  }
}
