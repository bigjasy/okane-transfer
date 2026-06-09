import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ApiErrorService } from '../services/api-error.service';
export const errorInterceptor: HttpInterceptorFn = (req, next) => next(req).pipe(catchError((err: HttpErrorResponse) => {
  const apiErrors = inject(ApiErrorService);
  const message = typeof err.error === 'object' && err.error?.message ? err.error.message : err.message || 'Erreur API';
  apiErrors.setError(message);
  return throwError(() => err);
}));
