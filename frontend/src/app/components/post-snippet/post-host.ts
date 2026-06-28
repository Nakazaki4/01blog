import { HttpClient } from '@angular/common/http';
import { inject, WritableSignal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PostReport, PostResponse } from './post-snippet';
import {
  PostCreateDialogComponent,
  PostCreateDialogData,
} from '../../features/post-create/post-create-dialog';

export abstract class PostHost {
  http = inject(HttpClient);
  protected dialog = inject(MatDialog);

  abstract onLikeToggled(postId: number): void;

  abstract onLikeStateChanged(change: { postId: number; isLiked: boolean; likeCount: number }): void;

  onCommentClicked(postId: number): void {
    // this.http.
  }

  abstract onCommentCountChanged(change: { postId: number; commentCount: number }): void;

  /**
   * Opens the edit dialog for the given post. On save, replaces the
   * post in the supplied signal with the updated version.
   */
  protected editPost(postId: number, posts: WritableSignal<PostResponse[]>): void {
    const current = posts().find((p) => p.id === postId);
    if (!current) return;
    const ref = this.dialog.open<
      PostCreateDialogComponent,
      PostCreateDialogData,
      PostResponse
    >(PostCreateDialogComponent, {
      data: { postId, description: current.description },
      panelClass: 'post-create-panel',
      autoFocus: false,
    });
    ref.afterClosed().subscribe((updated) => {
      if (!updated) return;
      posts.update((list) =>
        list.map((p) => (p.id === postId ? { ...p, ...updated } : p)),
      );
    });
  }

  onPostEdited(postId: number): void {
    console.log('edit post', postId);
  }

  abstract onPostDeleted(postId: number): void;

  onPostHidden(postId: number): void {
    console.log('hide post', postId);
  }

  onPostReported(report: PostReport): void {
    console.log('report submitted', report);
  }
}
