import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../auth.service'
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})

export class LoginComponent {
  private auth = inject(AuthService)
  private router = inject(Router)

  username = '';
  password = '';
  error = signal<string | null>(null);
  loading = signal(false);

  submit(): void {
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(["/"]),
      error: (err) => {
        this.error.set(err.error?.message || 'Login failed')
        this.loading.set(false);
      },
    })
  }
}
