import { Component, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { I18nService } from '../../../core/services/i18n.service';
import { Language } from '../../../core/models/enums';
@Component({ selector: 'app-navbar', standalone: true, imports: [RouterLink], template: `
<header class="navbar">
  <div><b>OkaneTransfer</b><small>Plateforme de transfert d’argent</small></div>
  <div class="right">
    <select [value]="lang.language()" (change)="setLang($any($event.target).value)"><option>FR</option><option>EN</option><option>AR</option></select>
    <span>{{ user()?.fullName }}</span><span class="role">{{ user()?.role }}</span>
    <button class="btn ghost" (click)="auth.logout()">Logout</button>
  </div>
</header>`, styles: [`.navbar{height:70px;background:white;border-bottom:1px solid #e2e8f0;display:flex;align-items:center;justify-content:space-between;padding:0 1.2rem;gap:1rem}.navbar b{display:block;font-size:1.1rem}.navbar small{color:#64748b}.right{display:flex;align-items:center;gap:.75rem;flex-wrap:wrap}.role{background:#dbeafe;color:#1e40af;border-radius:999px;padding:.25rem .6rem;font-size:.76rem;font-weight:800}@media(max-width:700px){.navbar{height:auto;padding:1rem}.right span{display:none}}`] })
export class NavbarComponent { user = computed(() => this.auth.user()); constructor(public auth: AuthService, public lang: I18nService) {} setLang(v: string) { this.lang.setLanguage(v as Language); } }
