import { Injectable, computed } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { ClientRegisterRequest, LoginRequest, LoginResponse, OtpVerifyRequest, RefreshTokenRequest, JwtResponse } from '../models/auth.models';
import { UserProfileResponse, UserSummaryResponse } from '../models/user.models';
import { ApiService } from './api.service';
import { MockApiService } from './mock-api.service';
import { TokenService } from './token.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly user = computed(() => this.tokens.currentUser());
  constructor(private readonly api: ApiService, private readonly mock: MockApiService, private readonly tokens: TokenService, private readonly router: Router) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    const source = environment.useMockApi ? this.mock.login(request) : this.api.post<LoginResponse | ApiResponse<LoginResponse>>('/auth/login', request).pipe(map(r => this.unwrap(r)));
    return source.pipe(tap(res => { if (!res.twoFactorRequired) this.persist(res); }));
  }
  registerClient(request: ClientRegisterRequest): Observable<UserSummaryResponse> {
    return environment.useMockApi ? this.mock.registerClient(request) : this.api.post<UserSummaryResponse | ApiResponse<UserSummaryResponse>>('/auth/register-client', request).pipe(map(r => this.unwrap(r)));
  }
  verifyOtp(request: OtpVerifyRequest): Observable<LoginResponse> {
    const source = environment.useMockApi ? this.mock.login({ email: 'admin@okane.ma', password: 'Password@123' }) : this.api.post<LoginResponse | ApiResponse<LoginResponse>>('/auth/verify-otp', request).pipe(map(r => this.unwrap(r)));
    return source.pipe(tap(res => this.persist(res)));
  }
  refresh(refreshToken?: string | null): Observable<JwtResponse> {
    const body: RefreshTokenRequest = { refreshToken: refreshToken || this.tokens.getRefreshToken() || '' };
    return this.api.post<JwtResponse | ApiResponse<JwtResponse>>('/auth/refresh', body).pipe(map(r => this.unwrap(r)), tap(t => this.tokens.saveTokens(t)));
  }
  me(): Observable<UserProfileResponse> {
    return this.api.get<UserProfileResponse | ApiResponse<UserProfileResponse>>('/auth/me').pipe(map(r => this.unwrap(r)));
  }
  logout(): void {
    const refreshToken = this.tokens.getRefreshToken();
    if (refreshToken && !environment.useMockApi) {
      this.api.post('/auth/logout', { refreshToken }).pipe(catchError(() => [])).subscribe();
    }
    this.tokens.clear();
    this.router.navigateByUrl('/auth/login');
  }
  redirectByRole(user: UserSummaryResponse): void {
    const target = user.role === 'ROLE_ADMIN' ? '/admin/dashboard' : user.role === 'ROLE_MANAGER' ? '/manager/dashboard' : user.role === 'ROLE_AGENT' ? '/agent/dashboard' : '/client/dashboard';
    this.router.navigateByUrl(target);
  }
  private persist(res: LoginResponse): void { this.tokens.saveTokens(res.tokens); this.tokens.saveUser(res.user); }
  private unwrap<T>(response: T | ApiResponse<T>): T { return response && typeof response === 'object' && 'data' in response && (response as ApiResponse<T>).data ? (response as ApiResponse<T>).data as T : response as T; }
}
