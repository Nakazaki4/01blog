import { HttpClient } from '@angular/common/http';
import { Component, Inject, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';

type ReportResponse = {
  id: number,
  reportedUserId: number,
  reason: String,
  status: String,
  createdAt: String,
}


@Component({
  selector: 'app-report-dialog',
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './report-dialog.html',
  styleUrl: './report-dialog.css',
})

export class ReportDialogComponent {
  private API_URL = "http://localhost:8080/api/reports";
  snackBar = inject(MatSnackBar)

  reason = signal('');
  http = inject(HttpClient);

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { postId: number },
    private dialogRef: MatDialogRef<ReportDialogComponent, string>
  ) { }

  submit(): void {
    const trimmed = this.reason().trim();

    if (!trimmed) return;

    this.dialogRef.close(trimmed);

    const payload = {
      postId: this.data.postId,
      reason: trimmed
    }

    this.http.post<ReportResponse>(`${this.API_URL}`, payload)
      .subscribe({
        next: (response) => {
          this.dialogRef.close(trimmed);
        },
        error: (err) => {
          console.log(err);
          
          let message = 'Something went wrong';

          if (err.status === 409) {
            message = 'You already reported this post';
          } else if (err.error?.message) {
            message = err.error.message;
          }

          this.snackBar.open(message, 'Close', {
            duration: 4000,
          });

          console.error(err);
        }
      })
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
