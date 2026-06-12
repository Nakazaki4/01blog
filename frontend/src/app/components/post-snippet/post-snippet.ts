import { Component, computed, inject, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { ReportDialogComponent } from './report-dialog/report-dialog';
import {
  PostDetailComponent,
  PostDetailData,
  PostDetailResult,
} from '../../features/post/post';
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
  selector: 'post-snippet',
  imports: [DatePipe, RouterLink, MatIconModule, MatButtonModule, MatMenuModule],
  templateUrl: './post-snippet.html',
  styleUrl: './post-snippet.css',
})
export class Post {
  post = input.required<PostResponse>();
  canInteract = input<boolean>(false);
  currentUserId = input<number | null>(null);
  isAdmin = input<boolean>(false);
  likePending = input<boolean>(false);

  likeToggled = output<number>();
  likeStateChanged = output<{ postId: number; isLiked: boolean; likeCount: number }>();
  commentClicked = output<number>();
  commentCountChanged = output<{ postId: number; commentCount: number }>();
  edited = output<number>();
  deleted = output<number>();
  reported = output<PostReport>();

  private dialog = inject(MatDialog);

  isOwner = computed(() => {
    const uid = this.currentUserId();
    return uid != null && uid === this.post().author.id;
  });

  canEdit = computed(() => this.isOwner());
  canDelete = computed(() => this.isOwner() || this.isAdmin());
  canReport = computed(() => this.canInteract() && (this.isAdmin() || !this.isOwner()));
  hasMenuActions = computed(() => this.canEdit() || this.canDelete() || this.canReport());

  snippet = computed(() => {
    const stripped = stripMarkdown(this.post().description ?? '');
    return stripped.length > SNIPPET_LENGTH
      ? stripped.slice(0, SNIPPET_LENGTH).trimEnd() + '…'
      : stripped;
  });

  openDetail(): void {
    const ref = this.dialog.open<PostDetailComponent, PostDetailData, PostDetailResult>(
      PostDetailComponent,
      {
        data: { post: this.post() },
        panelClass: 'post-detail-panel',
        autoFocus: false,
      },
    );
    ref.afterClosed().subscribe((result) => {
      if (!result) return;
      const current = this.post();
      if (result.commentCount !== current.commentCount) {
        this.commentCountChanged.emit({
          postId: result.postId,
          commentCount: result.commentCount,
        });
      }
      if (result.isLiked !== current.isLiked || result.likeCount !== current.likeCount) {
        this.likeStateChanged.emit({
          postId: result.postId,
          isLiked: result.isLiked,
          likeCount: result.likeCount,
        });
      }
    });
  }

  onLike(): void {
    if (!this.canInteract() || this.likePending()) return;
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
    const ref = this.dialog.open<ReportDialogComponent, { postId: number }, string>(ReportDialogComponent,
      {
        data: {
          postId: this.post().id
        }
      }
    );
    ref.afterClosed().subscribe((reason) => {
      if (!reason) return;
      this.reported.emit({ postId: this.post().id, reason });
    });
  }
}
