import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ApiErrorService } from '../services/api-error.service';
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const apiErrors = inject(ApiErrorService);
  return next(req).pipe(catchError((err: HttpErrorResponse) => {
    const message = typeof err.error === 'object' && err.error?.message ? err.error.message : err.message || 'Erreur API';
    apiErrors.setError(message);
    return throwError(() => err);
  }));
};
