import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PostResponse } from './post-snippet';

export interface CreatePostRequest {
  description: string;
}

export interface CommentAuthor {
  id: number;
  username: string;
  avatarUrl: string | null;
}

export interface CommentResponse {
  id: number;
  author: CommentAuthor;
  content: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class PostSnippetService {
  private http = inject(HttpClient);
  private API_URL = 'http://localhost:8080/api/posts';

  getFeed(page = 0, size = 20): Observable<PostResponse[]> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<PostResponse[]>(`${this.API_URL}/feed`, { params });
  }

  getByAuthor(userId: number | string, page = 0, size = 20): Observable<PostResponse[]> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<PostResponse[]>(
      `http://localhost:8080/api/users/${userId}/posts`,
      { params },
    );
  }

  getById(id: number): Observable<PostResponse> {
    return this.http.get<PostResponse>(`${this.API_URL}/${id}`);
  }

  create(req: CreatePostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(this.API_URL, req);
  }

  update(id: number, req: CreatePostRequest): Observable<PostResponse> {
    return this.http.put<PostResponse>(`${this.API_URL}/${id}`, req);
  }

  deleteComment(postId: number, commentId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.API_URL}/${postId}/comments/${commentId}`,
    );
  }

  uploadImage(formData: FormData): Observable<{ url: string }> {
    return this.http.post<{ url: string }>(`${this.API_URL}/upload-image`, formData);
  }

  like(postId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${postId}/like`, null);
  }

  unlike(postId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${postId}/like`);
  }

  listComments(postId: number, page = 0, size = 20): Observable<CommentResponse[]> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<CommentResponse[]>(
      `${this.API_URL}/${postId}/comments`,
      { params },
    );
  }

  addComment(postId: number, content: string): Observable<CommentResponse> {
    return this.http.post<CommentResponse>(
      `${this.API_URL}/${postId}/comments`,
      { content },
    );
  }
}