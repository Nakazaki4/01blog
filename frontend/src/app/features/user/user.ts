import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { UserProfile, UserService } from './user.service';

@Component({
  selector: 'app-user',
  imports: [],
  templateUrl: './user.html',
  styleUrl: './user.css',
})
export class User implements OnInit {
  private route = inject(ActivatedRoute);
  private userService = inject(UserService);
  private auth = inject(AuthService);

  profile = signal<UserProfile | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  isSelf = computed(() => {
    const me = this.auth.currentUser()?.userId;
    const id = this.profile()?.id;
    return me != null && id != null && String(me) === String(id);
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Missing user id');
      return;
    }
    this.loading.set(true);
    this.userService.getById(id).subscribe({
      next: (p) => {
        this.profile.set(p);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to load profile');
        this.loading.set(false);
      },
    });
  }

  logout(): void {
    this.auth.logout();
  }
}
