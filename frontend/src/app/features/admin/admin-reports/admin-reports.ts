import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminReport, AdminService, ReportStatus } from '../admin.service';

const PAGE_SIZE = 10;
type ReportFilter = ReportStatus | 'ALL';

@Component({
  selector: 'app-admin-reports',
  imports: [DatePipe, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './admin-reports.html',
  styleUrl: './admin-reports.css',
})
export class AdminReportsComponent implements OnInit {
  private admin = inject(AdminService);

  reports = signal<AdminReport[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  selectedStatus = signal<ReportFilter>('ALL');
  page = signal(0);
  totalElements = signal(0);
  actionPendingIds = signal<ReadonlySet<number>>(new Set());

  totalPages = computed(() => Math.max(1, Math.ceil(this.totalElements() / PAGE_SIZE)));

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(page = this.page()): void {
    this.loading.set(true);
    this.error.set(null);
    const status = this.selectedStatus() === 'ALL' ? undefined : this.selectedStatus() as ReportStatus;
    this.admin.listReports(page, PAGE_SIZE, status).subscribe({
      next: (result) => {
        this.reports.set(result);
        this.totalElements.set(result.length);
        this.page.set(result.length);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to load reports');
        this.loading.set(false);
      },
    });
  }

  onStatusChange(event: Event): void {
    this.selectedStatus.set((event.target as HTMLSelectElement).value as ReportFilter);
    this.loadReports(0);
  }

  updateStatus(report: AdminReport, status: ReportStatus): void {
    if (this.isActionPending(report.id)) return;
    this.setActionPending(report.id, true);
    this.admin.updateReportStatus(report.id, status).subscribe({
      next: (updated) => {
        this.reports.update((reports) =>
          reports.map((item) => item.id === report.id ? updated : item),
        );
        this.setActionPending(report.id, false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to update report');
        this.setActionPending(report.id, false);
      },
    });
  }

  nextPage(): void {
    if (this.page() + 1 >= this.totalPages()) return;
    this.loadReports(this.page() + 1);
  }

  previousPage(): void {
    if (this.page() === 0) return;
    this.loadReports(this.page() - 1);
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
