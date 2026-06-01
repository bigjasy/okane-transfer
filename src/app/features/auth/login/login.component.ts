import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/auth.models';
@Component({ selector: 'app-login', standalone: true, imports: [FormsModule, RouterLink], template: `
<div class="login card">
  <h1>OkaneTransfer</h1><p>Connexion sécurisée à la plateforme</p>
  @if(error){<div class="error-box">{{ error }}</div>}
  <form (ngSubmit)="submit()" class="grid">
    <label class="field">Email<input name="email" [(ngModel)]="form.email" /></label>
    <label class="field">Mot de passe<input name="password" type="password" [(ngModel)]="form.password" /></label>
    <button class="btn primary" type="submit">Se connecter</button>
  </form>
  <small>Comptes mock: admin@okane.ma, manager@okane.ma, agent@okane.ma, client@okane.ma / Password@123</small>
  <a routerLink="/auth/register">Créer un compte client</a>
</div>`, styles: [`.login{width:min(460px,100%);display:grid;gap:1rem}.login h1{margin:0;font-size:2rem}.login p{margin:0;color:#64748b}a{color:#1d4ed8;font-weight:800}`] })
export class LoginComponent { form: LoginRequest = { email: 'admin@okane.ma', password: 'Password@123' }; error=''; constructor(private readonly auth: AuthService) {} submit(){ this.error=''; this.auth.login(this.form).subscribe({ next: res => res.twoFactorRequired ? location.assign('/auth/verify-otp') : res.user && this.auth.redirectByRole(res.user), error: err => this.error = err?.error?.message || 'Connexion impossible' }); } }
