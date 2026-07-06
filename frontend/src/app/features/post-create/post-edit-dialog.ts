import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { PostCreateComponent } from './post-create';
import { PostResponse } from '../../components/post-snippet/post-snippet';

export interface PostEditDialogData {
  postId: number;
  description: string;
}

@Component({
  selector: 'app-post-edit-dialog',
  imports: [MatDialogModule, MatIconModule, MatButtonModule, PostCreateComponent],
  template: `
    <div class="dialog">
      <header class="dialog-header">
        <h2>Edit post</h2>
        <button mat-icon-button (click)="close()" aria-label="Close">
          <mat-icon>close</mat-icon>
        </button>
      </header>
      <app-post-create
        [postId]="data.postId"
        [initialDescription]="data.description"
        (postSaved)="onSaved($event)" />
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
export class PostEditDialogComponent {
  data = inject<PostEditDialogData>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<PostEditDialogComponent, PostResponse>);

  close(): void {
    this.dialogRef.close();
  }

  onSaved(post: PostResponse): void {
    this.dialogRef.close(post);
  }
}
