import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/enums';
import { TokenService } from '../services/token.service';
export const roleGuard: CanActivateFn = (route) => {
  const tokens = inject(TokenService); const router = inject(Router);
  const allowed = (route.data?.['roles'] ?? []) as Role[];
  const user = tokens.getUser();
  if (!allowed.length || (user && allowed.includes(user.role))) return true;
  return router.createUrlTree(['/auth/login']);
};
