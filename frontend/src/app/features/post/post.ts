import { Component, computed, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../auth/auth.service';
import { PostResponse } from '../../components/post-snippet/post-snippet';

export interface PostDetailData {
  post: PostResponse;
}

@Component({
  selector: 'app-post-detail',
  imports: [
    DatePipe,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './post.html',
  styleUrl: './post.css',
})
export class PostDetailComponent {
  private auth = inject(AuthService);
  private dialogRef = inject(MatDialogRef<PostDetailComponent>);

  post = inject<PostDetailData>(MAT_DIALOG_DATA).post;

  isAuthenticated = computed(() => !!this.auth.currentUser());

  close(): void {
    this.dialogRef.close();
  }
}
