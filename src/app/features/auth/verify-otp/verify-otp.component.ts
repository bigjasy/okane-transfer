import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
@Component({ selector:'app-verify-otp', standalone:true, imports:[FormsModule], template:`<div class="card"><h1>Vérification OTP</h1><form class="grid" (ngSubmit)="submit()"><label class="field">Temporary token<input name="tmp" [(ngModel)]="temporaryToken"></label><label class="field">Code OTP<input name="otp" [(ngModel)]="otpCode"></label><button class="btn primary">Vérifier</button></form></div>` })
export class VerifyOtpComponent { temporaryToken=''; otpCode='123456'; constructor(private auth: AuthService){} submit(){ this.auth.verifyOtp({ temporaryToken:this.temporaryToken, otpCode:this.otpCode, purpose:'LOGIN_2FA'}).subscribe(res=>res.user && this.auth.redirectByRole(res.user)); } }
