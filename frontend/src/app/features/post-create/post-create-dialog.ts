import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { PostCreateComponent } from './post-create';
import { PostResponse } from '../../components/post-snippet/post-snippet';

export interface PostCreateDialogData {
  postId: number;
  description: string;
}

@Component({
  selector: 'app-post-create-dialog',
  imports: [MatDialogModule, MatIconModule, MatButtonModule, PostCreateComponent],
  template: `
    <div class="dialog">
      <header class="dialog-header">
        <h2>{{ isEditing ? 'Edit post' : 'New post' }}</h2>
        <button mat-icon-button (click)="close()" aria-label="Close">
          <mat-icon>close</mat-icon>
        </button>
      </header>
      <app-post-create
        [editPostId]="data?.postId ?? null"
        [initialBody]="data?.description ?? ''"
        (postCreated)="onDone($event)"
        (postUpdated)="onDone($event)" />
    </div>
  `,
  styles: [`
    .dialog {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      padding: 1.5rem;
      width: 100%;
      height: 85vh;
      box-sizing: border-box;
    }
    .dialog-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .dialog-header h2 {
      margin: 0;
      font-size: 1.25rem;
    }
    .dialog app-post-create {
      flex: 1;
      display: flex;
      min-height: 0;
    }
  `],
})
export class PostCreateDialogComponent {
  private dialogRef = inject(MatDialogRef<PostCreateDialogComponent, PostResponse>);
  data = inject<PostCreateDialogData | null>(MAT_DIALOG_DATA, { optional: true });

  get isEditing(): boolean {
    return this.data?.postId != null;
  }

  close(): void {
    this.dialogRef.close();
  }

  onDone(post: PostResponse): void {
    this.dialogRef.close(post);
  }
}
