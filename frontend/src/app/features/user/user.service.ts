import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PostResponse } from '../../components/post-snippet/post-snippet';

export interface UserProfile {
  id: number;
  username: string;
  avatarUrl: string | null;
  bio: string | null;
  posts: PostResponse[];
  subscribersCount: number;
  followingCount: number;
  isSubscribed: boolean;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private API_URL = 'http://localhost:8080/api/users';

  getById(id: number | string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/${id}`);
  }

  subscribe(id: number | string): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/subscribe`, null);
  }

  unsubscribe(id: number | string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}/subscribe`);
  }
}
