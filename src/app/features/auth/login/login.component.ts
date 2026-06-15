import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/auth.models';
import { clearTwoFactorSession, saveTwoFactorSession } from '../../../core/constants/auth-session.constants';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
<div class="login card">
  <h1>OkaneTransfer</h1>
  <p>Connexion securisee a la plateforme</p>
  @if (error) { <div class="error-box">{{ error }}</div> }
  <form (ngSubmit)="submit()" class="grid">
    <label class="field">Email<input name="email" [(ngModel)]="form.email" autocomplete="username" /></label>
    <label class="field">Mot de passe<input name="password" type="password" [(ngModel)]="form.password" autocomplete="current-password" /></label>
    <button class="btn primary" type="submit" [disabled]="loading">{{ loading ? 'Connexion...' : 'Se connecter' }}</button>
  </form>
  <small>Comptes test: admin@okane.ma, manager@okane.ma, agent@okane.ma, client@okane.ma / Password@123</small>
  <a routerLink="/auth/register">Creer un compte client</a>
</div>`,
  styles: [`
    .login { width: min(460px, 100%); display: grid; gap: 1rem; }
    .login h1 { margin: 0; font-size: 2rem; }
    .login p { margin: 0; color: #64748b; }
    a { color: #1d4ed8; font-weight: 800; }
  `]
})
export class LoginComponent {
  form: LoginRequest = { email: 'admin@okane.ma', password: 'Password@123' };
  error = '';
  loading = false;

  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  submit(): void {
    this.error = '';
    this.loading = true;
    clearTwoFactorSession();

    this.auth.login(this.form).subscribe({
      next: res => {
        this.loading = false;
        if (res.twoFactorRequired) {
          if (!res.temporaryToken) {
            this.error = 'Jeton 2FA manquant. Reessayez.';
            return;
          }
          saveTwoFactorSession(res.temporaryToken, this.form.email, res.devOtpHint);
          this.router.navigate(['/auth/verify-otp']);
          return;
        }
        if (res.user) {
          this.auth.redirectByRole(res.user);
        }
      },
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || 'Connexion impossible';
      }
    });
  }
}
