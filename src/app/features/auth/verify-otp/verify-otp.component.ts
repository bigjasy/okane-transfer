import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import {
  AUTH_SESSION_KEYS,
  clearTwoFactorSession
} from '../../../core/constants/auth-session.constants';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
<div class="card verify-card">
  <h1>Verification OTP</h1>
  <p class="subtitle">Authentification a deux facteurs pour <strong>{{ email || 'votre compte' }}</strong></p>

  @if (error) { <div class="error-box">{{ error }}</div> }
  @if (devHint) { <div class="hint-box">Code dev (notification.expose-otp): <strong>{{ devHint }}</strong></div> }

  <form class="grid" (ngSubmit)="submit()">
    <label class="field">
      Code OTP
      <input name="otp" [(ngModel)]="otpCode" maxlength="6" autocomplete="one-time-code" required />
    </label>
    <button class="btn primary" type="submit" [disabled]="loading || !temporaryToken">
      {{ loading ? 'Verification...' : 'Verifier et continuer' }}
    </button>
  </form>

  <a routerLink="/auth/login" (click)="cancel()">Retour a la connexion</a>
</div>`,
  styles: [`
    .verify-card { max-width: 420px; display: grid; gap: 1rem; }
    .subtitle { margin: 0; color: #64748b; }
    .hint-box { padding: .75rem 1rem; border-radius: .75rem; background: #ecfdf5; color: #065f46; }
    a { color: #1d4ed8; font-weight: 700; }
  `]
})
export class VerifyOtpComponent implements OnInit {
  temporaryToken = '';
  email = '';
  otpCode = '';
  devHint = '';
  error = '';
  loading = false;

  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  ngOnInit(): void {
    this.temporaryToken = sessionStorage.getItem(AUTH_SESSION_KEYS.twoFactorToken) ?? '';
    this.email = sessionStorage.getItem(AUTH_SESSION_KEYS.twoFactorEmail) ?? '';
    this.devHint = sessionStorage.getItem(AUTH_SESSION_KEYS.twoFactorHint) ?? '';

    if (!this.temporaryToken) {
      this.router.navigate(['/auth/login']);
    }
  }

  submit(): void {
    if (!this.temporaryToken) {
      this.error = 'Session 2FA expiree. Reconnectez-vous.';
      return;
    }
    this.error = '';
    this.loading = true;

    this.auth.verifyOtp({
      temporaryToken: this.temporaryToken,
      otpCode: this.otpCode.trim(),
      purpose: 'LOGIN_2FA'
    }).subscribe({
      next: res => {
        this.loading = false;
        clearTwoFactorSession();
        if (res.user) {
          this.auth.redirectByRole(res.user);
        }
      },
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || 'Code OTP invalide ou expire.';
      }
    });
  }

  cancel(): void {
    clearTwoFactorSession();
  }
}
