import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../features/auth/auth.service';
import { PostCreateDialogComponent } from '../../features/post-create/post-create-dialog';
import { PostResponse } from '../post-snippet/post-snippet';
import { PostEventsService } from '../../shared/post-events.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, MatIconModule, MatButtonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class NavbarComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private postEvents = inject(PostEventsService);

  user = this.auth.currentUser;
  isAuthenticated = computed(() => !!this.user());

  openCreatePost(): void {
    const ref = this.dialog.open<PostCreateDialogComponent, void, PostResponse>(
      PostCreateDialogComponent,
      { panelClass: 'post-create-panel', autoFocus: false },
    );
    ref.afterClosed().subscribe((post) => {
      if (post) this.postEvents.emitCreated(post);
    });
  }

  goToProfile(): void {
    const id = this.user()?.userId;
    if (id != null) this.router.navigate(['/profile', id]);
  }
}
