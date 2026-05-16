import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './features/auth/auth.service';
import { inject } from '@angular/core';

export const adminGuard: CanActivateFn = () => {
  const user = inject(AuthService)
  const router = inject(Router)

  return user.isAdmin() ? true : router.navigate(["/unauthorized"])
};