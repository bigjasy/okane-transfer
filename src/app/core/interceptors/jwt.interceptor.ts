import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';
const PUBLIC = ['/auth/login', '/auth/register-client', '/auth/verify-otp', '/auth/refresh', '/otp/request', '/otp/verify'];
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  if (PUBLIC.some(url => req.url.includes(url))) return next(req);
  const token = inject(TokenService).getAccessToken();
  return token ? next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })) : next(req);
};
