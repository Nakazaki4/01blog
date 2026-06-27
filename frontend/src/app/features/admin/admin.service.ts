import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PostResponse } from '../../components/post-snippet/post-snippet';

export interface AdminStats {
  totalUsers: number;
  totalPosts: number;
  totalReports: number;
  totalPendingReports: number;
}

export interface AdminUser {
  id: number;
  username: string;
  email: string;
  role: string;
  banned: boolean;
  postCount?: number;
  reportCount?: number;
  createdAt?: string;
}

export interface AdminPost extends PostResponse {
  reportCount?: number;
  mediaUrl?: string | null;
  mediaType?: string | null;
  updatedAt?: string | null;
  deleted?: boolean;
  hidden?: boolean;
}

export type ReportStatus = 'PENDING' | 'REVIEWED' | 'DISMISSED';

export interface AdminReportUser {
  id: number;
  username: string;
}

export interface AdminReport {
  id: number;
  reporter: AdminReportUser;
  reportedUser: AdminReportUser;
  reason: string;
  status: ReportStatus;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private API_URL = 'http://localhost:8080/api/admin';

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.API_URL}/stats`);
  }

  listUsers(page = 0, size = 20, search = ''): Observable<AdminUser[]> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search.trim()) params = params.set('search', search.trim());
    return this.http.get<AdminUser[]>(`${this.API_URL}/users`, { params });
  }

  listPosts(page = 0, size = 20): Observable<AdminPost[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<AdminPost[]>(`${this.API_URL}/posts`, { params });
  }

  listReports(
    page = 0,
    size = 20,
    status?: ReportStatus,
  ): Observable<AdminReport[]> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<AdminReport[]>(`${this.API_URL}/reports`, { params });
  }

  banUser(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/users/${id}/ban`, null);
  }

  unbanUser(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/users/${id}/unban`, null);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/users/${id}`);
  }

  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/posts/${id}`);
  }

  hidePost(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/posts/${id}/hide`, null);
  }

  unhidePost(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/posts/${id}/unhide`, null);
  }

  updateReportStatus(id: number, status: ReportStatus): Observable<AdminReport> {
    return this.http.patch<AdminReport>(`${this.API_URL}/reports/${id}`, { status });
  }
}
