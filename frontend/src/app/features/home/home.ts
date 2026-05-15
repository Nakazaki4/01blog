import { Component, inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-home',
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent {
  private auth = inject(AuthService);
  user = this.auth.currentUser;

  logout(): void {
    this.auth.logout();
  }
}
