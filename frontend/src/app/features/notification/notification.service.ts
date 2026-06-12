import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  DestroyRef,
  Inject,
  Injectable,
  PLATFORM_ID,
  effect,
  inject,
  signal,
} from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export type NotificationType =
  | 'NEW_POST'
  | 'NEW_COMMENT'
  | 'NEW_LIKE'
  | 'NEW_SUBSCRIBER';

export interface NotificationActor {
  id: number;
  username: string;
  avatarUrl: string | null;
}

export interface NotificationResponse {
  id: number;
  actor: NotificationActor;
  type: NotificationType;
  postId: number | null;
  isRead: boolean;
  createdAt: string;
}

const POLL_INTERVAL_MS = 30_000;

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private destroyRef = inject(DestroyRef);
  private isBrowser: boolean;
  private API_URL = 'http://localhost:8080/api/notifications';

  unreadCount = signal<number>(0);

  private pollHandle: ReturnType<typeof setInterval> | null = null;

  constructor(@Inject(PLATFORM_ID) platformId: object) {
    this.isBrowser = isPlatformBrowser(platformId);

    effect(() => {
      const user = this.auth.currentUser();
      if (user && this.isBrowser) {
        this.startPolling();
      } else {
        this.stopPolling();
        this.unreadCount.set(0);
      }
    });

    this.destroyRef.onDestroy(() => this.stopPolling());
  }

  list(page = 0, size = 20): Observable<NotificationResponse[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<NotificationResponse[]>(this.API_URL, { params });
  }

  markRead(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/read`, null);
  }

  markAllRead(): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/read-all`, null);
  }

  refreshUnreadCount(): void {
    if (!this.auth.currentUser()) return;
    this.http
      .get<{ count: number }>(`${this.API_URL}/unread-count`)
      .subscribe({
        next: (res) => this.unreadCount.set(res.count),
        error: () => {},
      });
  }

  private startPolling(): void {
    if (this.pollHandle != null) return;
    this.refreshUnreadCount();
    this.pollHandle = setInterval(() => this.refreshUnreadCount(), POLL_INTERVAL_MS);
  }

  private stopPolling(): void {
    if (this.pollHandle != null) {
      clearInterval(this.pollHandle);
      this.pollHandle = null;
    }
  }
}