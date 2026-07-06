import { HttpClient } from '@angular/common/http';
import { PostReport, PostResponse } from './post-snippet';
import { inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import {
  PostEditDialogComponent,
  PostEditDialogData,
} from '../../features/post-create/post-edit-dialog';
import { PostSnippetService } from './post-snippet.service';

export abstract class PostHost {
  http = inject(HttpClient);
  private editDialog = inject(MatDialog);
  private postSnippetService = inject(PostSnippetService);

  abstract onLikeToggled(postId: number): void;

  abstract onLikeStateChanged(change: { postId: number; isLiked: boolean; likeCount: number }): void;

  onCommentClicked(postId: number): void {
    // this.http.
  }

  abstract onCommentCountChanged(change: { postId: number; commentCount: number }): void;

  onPostEdited(post: PostResponse): void {
    this.postSnippetService.getById(post.id).subscribe({
      next: (full) => {
        const ref = this.editDialog.open<PostEditDialogComponent, PostEditDialogData, PostResponse>(
          PostEditDialogComponent,
          {
            data: { postId: full.id, description: full.description },
            panelClass: 'post-detail-panel',
            autoFocus: false,
          },
        );
        ref.afterClosed().subscribe((updated) => {
          if (!updated) return;
          this.onPostUpdated(updated);
        });
      },
    });
  }

  onPostUpdated(post: PostResponse): void {
    // Override in subclasses to update the local post list.
  }

  abstract onPostDeleted(postId: number): void;

  onPostHidden(postId: number): void {
    console.log('hide post', postId);
  }

  onPostReported(report: PostReport): void {
    console.log('report submitted', report);
  }
}
