import { Component, computed, inject, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { ReportDialogComponent } from './report-dialog/report-dialog';
import { PostDetailComponent, PostDetailData } from '../../features/post/post';
import { stripMarkdown } from '../../shared/markdown';

export interface PostAuthor {
  id: number;
  username: string;
  avatarUrl: string | null;
}

export interface PostResponse {
  id: number;
  author: PostAuthor;
  description: string;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  createdAt: string;
}

export interface PostReport {
  postId: number;
  reason: string;
}

const SNIPPET_LENGTH = 180;

@Component({
  selector: 'app-post-snippet',
  imports: [DatePipe, RouterLink, MatIconModule, MatButtonModule, MatMenuModule],
  templateUrl: './post-snippet.html',
  styleUrl: './post-snippet.css',
})
export class Post {
  post = input.required<PostResponse>();
  canInteract = input<boolean>(false);
  currentUserId = input<number | null>(null);

  likeToggled = output<number>();
  commentClicked = output<number>();
  edited = output<number>();
  deleted = output<number>();
  reported = output<PostReport>();

  private dialog = inject(MatDialog);

  isOwner = computed(() => {
    const uid = this.currentUserId();
    return uid != null && uid === this.post().author.id;
  });

  snippet = computed(() => {
    const stripped = stripMarkdown(this.post().description ?? '');
    return stripped.length > SNIPPET_LENGTH
      ? stripped.slice(0, SNIPPET_LENGTH).trimEnd() + '…'
      : stripped;
  });

  openDetail(): void {
    this.dialog.open<PostDetailComponent, PostDetailData>(PostDetailComponent, {
      data: { post: this.post() },
      panelClass: 'post-detail-panel',
      autoFocus: false,
    });
  }

  onLike(): void {
    if (!this.canInteract()) return;
    this.likeToggled.emit(this.post().id);
  }

  onComment(): void {
    if (!this.canInteract()) return;
    this.commentClicked.emit(this.post().id);
  }

  onEdit(): void {
    this.edited.emit(this.post().id);
  }

  onDelete(): void {
    this.deleted.emit(this.post().id);
  }

  onReport(): void {
    const ref = this.dialog.open<ReportDialogComponent, void, string>(ReportDialogComponent);
    ref.afterClosed().subscribe((reason) => {
      if (!reason) return;
      this.reported.emit({ postId: this.post().id, reason });
    });
  }
}
