import { Component, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TokenService } from '../../../core/services/token.service';
import { Role } from '../../../core/models/enums';
interface Item { label: string; path: string; roles: Role[]; }
@Component({ selector: 'app-sidebar', standalone: true, imports: [RouterLink, RouterLinkActive], template: `
<aside class="sidebar">
  <div class="brand">💸 <span>OkaneTransfer</span></div>
  <nav>@for (item of items(); track item.path) { <a [routerLink]="item.path" routerLinkActive="active">{{ item.label }}</a> }</nav>
</aside>`, styles: [`.sidebar{min-height:100vh;background:#0f172a;color:white;padding:1rem;display:flex;flex-direction:column;gap:1.25rem}.brand{font-size:1.2rem;font-weight:900;padding:.8rem}.brand span{margin-left:.35rem}nav{display:grid;gap:.35rem}a{padding:.75rem .9rem;border-radius:.85rem;color:#cbd5e1}a:hover,a.active{background:#1d4ed8;color:white}@media(max-width:900px){.sidebar{min-height:auto}.brand span{display:inline}nav{grid-template-columns:repeat(2,minmax(0,1fr))}}`] })
export class SidebarComponent {
  constructor(private readonly tokens: TokenService) {}
  private all: Item[] = [
    { label: 'Admin Dashboard', path: '/admin/dashboard', roles: ['ROLE_ADMIN'] }, { label: 'Utilisateurs', path: '/admin/users', roles: ['ROLE_ADMIN'] }, { label: 'Agences', path: '/admin/agencies', roles: ['ROLE_ADMIN'] }, { label: 'Pays', path: '/admin/countries', roles: ['ROLE_ADMIN'] }, { label: 'Devises', path: '/admin/currencies', roles: ['ROLE_ADMIN'] }, { label: 'Taux', path: '/admin/exchange-rates', roles: ['ROLE_ADMIN'] }, { label: 'Corridors', path: '/admin/corridors', roles: ['ROLE_ADMIN'] }, { label: 'Frais', path: '/admin/fee-grids', roles: ['ROLE_ADMIN'] }, { label: 'Conformité', path: '/admin/compliance', roles: ['ROLE_ADMIN'] }, { label: 'Rapports', path: '/admin/reports', roles: ['ROLE_ADMIN'] }, { label: 'Audit', path: '/admin/audit', roles: ['ROLE_ADMIN'] },
    { label: 'Manager Dashboard', path: '/manager/dashboard', roles: ['ROLE_MANAGER'] }, { label: 'Agents', path: '/manager/agents', roles: ['ROLE_MANAGER'] }, { label: 'Caisses', path: '/manager/cash-registers', roles: ['ROLE_MANAGER'] }, { label: 'Rapports agence', path: '/manager/reports', roles: ['ROLE_MANAGER'] },
    { label: 'Agent Dashboard', path: '/agent/dashboard', roles: ['ROLE_AGENT'] }, { label: 'Transferts', path: '/agent/transfers', roles: ['ROLE_AGENT'] }, { label: 'Créer transfert', path: '/agent/create-transfer', roles: ['ROLE_AGENT'] }, { label: 'Simulation frais', path: '/agent/fee-simulation', roles: ['ROLE_AGENT'] }, { label: 'Payout', path: '/agent/payout', roles: ['ROLE_AGENT'] }, { label: 'Caisse', path: '/agent/cash-register', roles: ['ROLE_AGENT'] },
    { label: 'Client Dashboard', path: '/client/dashboard', roles: ['ROLE_CLIENT'] }, { label: 'Profil', path: '/client/profile', roles: ['ROLE_CLIENT'] }, { label: 'Bénéficiaires', path: '/client/beneficiaries', roles: ['ROLE_CLIENT'] }, { label: 'Suivi transfert', path: '/client/transfer-tracking', roles: ['ROLE_CLIENT'] },
    { label: 'Mobile Money', path: '/mobile-money', roles: ['ROLE_ADMIN','ROLE_MANAGER','ROLE_AGENT','ROLE_CLIENT'] }, { label: 'Chatbot', path: '/chatbot', roles: ['ROLE_ADMIN','ROLE_MANAGER','ROLE_AGENT','ROLE_CLIENT'] }
  ];
  items = computed(() => { const role = this.tokens.currentUser()?.role; return role ? this.all.filter(i => i.roles.includes(role)) : []; });
}
