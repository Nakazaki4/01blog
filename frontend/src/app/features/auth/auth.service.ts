import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Inject, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export type AuthResponse = {
  token: String,
  userId: String,
  username: String,
  role: String
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private API_URL = 'http://localhost:8080/api/auth';
  private TOKEN_KEY = 'user-basic-details'
  private isBrowser = isPlatformBrowser(inject(PLATFORM_ID))

  currentUser = signal<AuthResponse | null>(this.loadFromStorage());

  http = inject(HttpClient)
  router = inject(Router)

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`,
      { username, password })
      .pipe(tap(res => this.handleAuth(res)))
  }

  register(payload: {
    username: string;
    email: string;
    password: string;
  }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/signup`, payload)
      .pipe(tap(res => this.handleAuth(res)))
  }

  logout() {
    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
    this.currentUser.set(null);
    this.router.navigate(["/"]);
  }

  getToken(): string {
    if (!this.isBrowser) return '';
    const raw = localStorage.getItem(this.TOKEN_KEY);
    return raw ? JSON.parse(raw).token ?? '' : '';
  }

  isLoggedIn(): boolean {
    return !!this.getToken()
  }
  isAdmin(): boolean {
    return this.currentUser()?.role === "ADMIN"
  }

  private handleAuth(response: AuthResponse) {
    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, JSON.stringify(response));
    }
    this.currentUser.set(response);
  }

  private loadFromStorage(): AuthResponse | null {
    if (!this.isBrowser) return null

    const raw = localStorage.getItem(this.TOKEN_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
