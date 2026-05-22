import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PostResponse } from './post-snippet';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CreatePostRequest {
  description: string;
}

@Injectable({ providedIn: 'root' })
export class PostService {
  private http = inject(HttpClient);
  private API_URL = 'http://localhost:8080/api/posts';

  getFeed(page = 0, size = 20): Observable<PageResponse<PostResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<PostResponse>>(`${this.API_URL}/feed`, { params });
  }

  getById(id: number): Observable<PostResponse> {
    return this.http.get<PostResponse>(`${this.API_URL}/${id}`);
  }

  create(req: CreatePostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(this.API_URL, req);
  }

  uploadImage(formData: FormData): Observable<{ url: string }> {
    return this.http.post<{ url: string }>(`${this.API_URL}/upload-image`, formData);
  }
}
