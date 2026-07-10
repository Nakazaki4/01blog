import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface SearchUser {
  id: number;
  username: string;
  avatarUrl: string | null;
}

@Injectable({ providedIn: 'root' })
export class SearchService {
  private http = inject(HttpClient);
  private API_URL = 'http://localhost:8080/api/search';

  searchUsers(keyword: string): Observable<SearchUser[]> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<SearchUser[]>(this.API_URL, { params });
  }
}
