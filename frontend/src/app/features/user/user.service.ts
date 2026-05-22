import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface UserProfile {
  id: number;
  username: string;
  email?: string;
  bio?: string | null;
  avatarUrl?: string | null;
  role?: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private API_URL = 'http://localhost:8080/api/users';

  getById(id: number | string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/${id}`);
  }
}
