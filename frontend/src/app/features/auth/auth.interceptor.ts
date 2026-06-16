import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { tap } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const snackBar = inject(MatSnackBar);
  const user = auth.currentUser();

  if (user?.token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${user.token}` }
    });
  }

  return next(req).pipe(
    tap({
      error: (err) => {
        if (err.status === 403 && auth.isLoggedIn()) {
          const message = err.error?.message ?? 'Your account has been banned';
          snackBar.open(message, 'Close', { duration: 5000 });
          auth.logout();
        }
      }
    })
  );
};
