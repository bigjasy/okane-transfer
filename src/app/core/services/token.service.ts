import { Injectable, signal } from '@angular/core';
import { JwtResponse } from '../models/auth.models';
import { UserSummaryResponse } from '../models/user.models';

const ACCESS = 'okane.accessToken';
const REFRESH = 'okane.refreshToken';
const USER = 'okane.user';

@Injectable({ providedIn: 'root' })
export class TokenService {
  readonly currentUser = signal<UserSummaryResponse | null>(this.getUser());

  saveTokens(tokens?: JwtResponse): void {
    if (!tokens) return;
    localStorage.setItem(ACCESS, tokens.accessToken);
    localStorage.setItem(REFRESH, tokens.refreshToken);
  }
  getAccessToken(): string | null { return localStorage.getItem(ACCESS); }
  getRefreshToken(): string | null { return localStorage.getItem(REFRESH); }
  saveUser(user?: UserSummaryResponse): void {
    if (!user) return;
    localStorage.setItem(USER, JSON.stringify(user));
    this.currentUser.set(user);
  }
  getUser(): UserSummaryResponse | null {
    const raw = localStorage.getItem(USER);
    if (!raw) return null;
    try { return JSON.parse(raw) as UserSummaryResponse; } catch { return null; }
  }
  clear(): void {
    localStorage.removeItem(ACCESS);
    localStorage.removeItem(REFRESH);
    localStorage.removeItem(USER);
    this.currentUser.set(null);
  }
  isAuthenticated(): boolean { return !!this.getAccessToken(); }
}
