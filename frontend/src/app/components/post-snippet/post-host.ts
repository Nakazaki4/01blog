import { PostReport } from './post-snippet';

export abstract class PostHost {
  onLikeToggled(postId: number): void {
    console.log('like toggled for post', postId);
  }

  onCommentClicked(postId: number): void {
    console.log('comment clicked for post', postId);
  }

  onPostEdited(postId: number): void {
    console.log('edit post', postId);
  }

  abstract onPostDeleted(postId: number): void;

  onPostReported(report: PostReport): void {
    console.log('report submitted', report);
  }
}
