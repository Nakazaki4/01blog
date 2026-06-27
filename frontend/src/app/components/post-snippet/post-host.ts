import { HttpClient } from '@angular/common/http';
import { PostReport } from './post-snippet';
import { inject } from '@angular/core';

export abstract class PostHost {
  http = inject(HttpClient);

  abstract onLikeToggled(postId: number): void;

  abstract onLikeStateChanged(change: { postId: number; isLiked: boolean; likeCount: number }): void;

  onCommentClicked(postId: number): void {
    // this.http.
  }

  abstract onCommentCountChanged(change: { postId: number; commentCount: number }): void;

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
