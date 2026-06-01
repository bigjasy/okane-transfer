import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';
export const refreshTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService); const tokens = inject(TokenService);
  if (req.url.includes('/auth/refresh')) return next(req);
  return next(req).pipe(catchError((err: HttpErrorResponse) => {
    if (err.status !== 401 || !tokens.getRefreshToken()) return throwError(() => err);
    return auth.refresh().pipe(switchMap(newTokens => next(req.clone({ setHeaders: { Authorization: `Bearer ${newTokens.accessToken}` } }))), catchError(refreshErr => { auth.logout(); return throwError(() => refreshErr); }));
  }));
};
