import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../auth/auth.service';
import { stripMarkdown } from '../../../shared/markdown';
import { AdminPost, AdminReport, AdminService, AdminUser } from '../admin.service';

interface DashboardTotals {
  users: number;
  posts: number;
  reports: number;
  pendingReports: number;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboardComponent implements OnInit {
  private auth = inject(AuthService);
  private admin = inject(AdminService);

  user = this.auth.currentUser;
  loading = signal(false);
  error = signal<string | null>(null);
  totals = signal<DashboardTotals>({
    users: 0,
    posts: 0,
    reports: 0,
    pendingReports: 0,
  });
  recentUsers = signal<AdminUser[]>([]);
  recentPosts = signal<AdminPost[]>([]);
  recentReports = signal<AdminReport[]>([]);

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      users: this.admin.listUsers(0, 5),
      posts: this.admin.listPosts(0, 5),
      reports: this.admin.listReports(0, 5),
      pendingReports: this.admin.listReports(0, 1, 'PENDING'),
    }).subscribe({
      next: ({ users, posts, reports, pendingReports }) => {
        this.totals.set({
          users: users.length,
          posts: posts.length,
          reports: reports.length,
          pendingReports: pendingReports.length,
        });
        this.recentUsers.set(users);
        this.recentPosts.set(posts);
        this.recentReports.set(reports);
        console.log(reports)
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to load admin dashboard');
        this.loading.set(false);
      },
    });
  }

  logout(): void {
    this.auth.logout();
  }

  postSnippet(post: AdminPost): string {
    const text = stripMarkdown(post.description ?? '');
    return text.length > 96 ? `${text.slice(0, 96).trimEnd()}...` : text;
  }
}
