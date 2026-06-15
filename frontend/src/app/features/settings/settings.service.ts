import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private http = inject(HttpClient);
  private BASE = 'http://localhost:8080/api/settings';

  changeEmail(email: string): Observable<void> {
    return this.http.patch<void>(`${this.BASE}/email`, { email });
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.patch<void>(`${this.BASE}/password`, { currentPassword, newPassword });
  }

  changeAvatar(file: File): Observable<void> {
    const form = new FormData();
    form.append('avatar', file);
    return this.http.patch<void>(`${this.BASE}/avatar`, form);
  }

  deleteAccount(): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/account`);
  }
}
