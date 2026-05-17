import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { retry } from 'rxjs';
import { count } from 'console';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService)
  const user = auth.currentUser();

  if (user?.token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${user.token}` }
    });
  }

  return next(req);
};
