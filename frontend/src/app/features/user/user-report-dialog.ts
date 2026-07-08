import { HttpClient } from '@angular/common/http';
import { Component, Inject, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';

type ReportResponse = {
  id: number;
  reportedUserId: number;
  reason: string;
  status: string;
  createdAt: string;
};

@Component({
  selector: 'app-user-report-dialog',
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './user-report-dialog.html',
  styleUrl: './user-report-dialog.css',
})
export class UserReportDialogComponent {
  private API_URL = 'http://localhost:8080/api/reports/user';
  private snackBar = inject(MatSnackBar);
  private http = inject(HttpClient);

  reason = signal('');
  submitting = signal(false);

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { reportedUserId: number },
    private dialogRef: MatDialogRef<UserReportDialogComponent, string>,
  ) {}

  submit(): void {
    const trimmed = this.reason().trim();
    if (!trimmed || this.submitting()) return;

    this.submitting.set(true);
    const payload = {
      reportedUserId: String(this.data.reportedUserId),
      reason: trimmed,
    };

    this.http.post<ReportResponse>(this.API_URL, payload).subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open('Report submitted', 'Close', { duration: 3000 });
        this.dialogRef.close(trimmed);
      },
      error: (err) => {
        this.submitting.set(false);
        let message = 'Something went wrong';
        if (err.status === 409) {
          message = 'You already reported this user';
        } else if (err.error?.message) {
          message = err.error.message;
        }
        this.snackBar.open(message, 'Close', { duration: 4000 });
      },
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
