import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../auth/auth.service';
import { SettingsService } from './settings.service';

@Component({
  selector: 'app-settings',
  imports: [FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class SettingsComponent {
  private settings = inject(SettingsService);
  private auth = inject(AuthService);
  private router = inject(Router);

  email = signal('');
  currentPassword = signal('');
  newPassword = signal('');
  confirmPassword = signal('');
  avatarPreview = signal<string | null>(null);
  avatarFile = signal<File | null>(null);

  emailError = signal<string | null>(null);
  emailSuccess = signal(false);
  passwordError = signal<string | null>(null);
  passwordSuccess = signal(false);
  avatarError = signal<string | null>(null);
  avatarSuccess = signal(false);

  loadingEmail = signal(false);
  loadingPassword = signal(false);
  loadingAvatar = signal(false);
  confirmDelete = signal(false);

  onAvatarSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.avatarFile.set(file);
    this.avatarPreview.set(URL.createObjectURL(file));
  }

  submitEmail(): void {
    this.emailError.set(null);
    this.emailSuccess.set(false);
    this.loadingEmail.set(true);
    this.settings.changeEmail(this.email()).subscribe({
      next: () => { this.emailSuccess.set(true); this.loadingEmail.set(false); },
      error: (e) => { this.emailError.set(e.error?.message ?? 'Failed to update email'); this.loadingEmail.set(false); },
    });
  }

  submitPassword(): void {
    this.passwordError.set(null);
    this.passwordSuccess.set(false);
    if (this.newPassword() !== this.confirmPassword()) {
      this.passwordError.set('Passwords do not match');
      return;
    }
    this.loadingPassword.set(true);
    this.settings.changePassword(this.currentPassword(), this.newPassword()).subscribe({
      next: () => { this.passwordSuccess.set(true); this.loadingPassword.set(false); },
      error: (e) => { this.passwordError.set(e.error?.message ?? 'Failed to update password'); this.loadingPassword.set(false); },
    });
  }

  submitAvatar(): void {
    const file = this.avatarFile();
    if (!file) return;
    this.avatarError.set(null);
    this.avatarSuccess.set(false);
    this.loadingAvatar.set(true);
    this.settings.changeAvatar(file).subscribe({
      next: () => { this.avatarSuccess.set(true); this.loadingAvatar.set(false); },
      error: (e) => { this.avatarError.set(e.error?.message ?? 'Failed to update avatar'); this.loadingAvatar.set(false); },
    });
  }

  deleteAccount(): void {
    this.settings.deleteAccount().subscribe({
      next: () => { this.auth.logout(); this.router.navigate(['/']); },
    });
  }
}
