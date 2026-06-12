import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService, AdminUser } from '../admin.service';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [DatePipe, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.css',
})
export class AdminUsersComponent implements OnInit {
  private admin = inject(AdminService);

  users = signal<AdminUser[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  search = signal('');
  page = signal(0);
  totalElements = signal(0);
  actionPendingIds = signal<ReadonlySet<number>>(new Set());

  totalPages = computed(() => Math.max(1, Math.ceil(this.totalElements() / PAGE_SIZE)));

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(page = this.page()): void {
    this.loading.set(true);
    this.error.set(null);
    this.admin.listUsers(page, PAGE_SIZE, this.search()).subscribe({
      next: (result) => {
        this.users.set(result);
        this.totalElements.set(result.length);
        this.page.set(result.length);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to load users');
        this.loading.set(false);
      },
    });
  }

  onSearchInput(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  applySearch(): void {
    this.loadUsers(0);
  }

  clearSearch(): void {
    this.search.set('');
    this.loadUsers(0);
  }

  toggleBan(user: AdminUser): void {
    if (this.isActionPending(user.id)) return;
    this.setActionPending(user.id, true);
    const request = user.banned ? this.admin.unbanUser(user.id) : this.admin.banUser(user.id);
    request.subscribe({
      next: () => {
        this.users.update((users) =>
          users.map((item) =>
            item.id === user.id ? { ...item, banned: !user.banned } : item,
          ),
        );
        this.setActionPending(user.id, false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to update user');
        this.setActionPending(user.id, false);
      },
    });
  }

  deleteUser(user: AdminUser): void {
    if (this.isActionPending(user.id)) return;
    if (!confirm(`Delete ${user.username} and their content?`)) return;
    this.setActionPending(user.id, true);
    this.admin.deleteUser(user.id).subscribe({
      next: () => {
        this.users.update((users) => users.filter((item) => item.id !== user.id));
        this.totalElements.update((count) => Math.max(0, count - 1));
        this.setActionPending(user.id, false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to delete user');
        this.setActionPending(user.id, false);
      },
    });
  }

  nextPage(): void {
    if (this.page() + 1 >= this.totalPages()) return;
    this.loadUsers(this.page() + 1);
  }

  previousPage(): void {
    if (this.page() === 0) return;
    this.loadUsers(this.page() - 1);
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
