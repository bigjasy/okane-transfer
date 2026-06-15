import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { PublicLayoutComponent } from './layouts/public-layout/public-layout.component';
import { SecuredLayoutComponent } from './layouts/secured-layout/secured-layout.component';

export const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'auth', component: PublicLayoutComponent, children: [
    { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
    { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
    { path: 'verify-otp', loadComponent: () => import('./features/auth/verify-otp/verify-otp.component').then(m => m.VerifyOtpComponent) }
  ]},
  { path: 'admin', component: SecuredLayoutComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_ADMIN'] }, children: [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: 'dashboard', loadComponent: () => import('./features/admin/dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
    { path: 'users', loadComponent: () => import('./features/admin/users/users.component').then(m => m.UsersComponent) },
    { path: 'agencies', loadComponent: () => import('./features/admin/agencies/agencies.component').then(m => m.AgenciesComponent) },
    { path: 'countries', loadComponent: () => import('./features/admin/countries/countries.component').then(m => m.CountriesComponent) },
    { path: 'currencies', loadComponent: () => import('./features/admin/currencies/currencies.component').then(m => m.CurrenciesComponent) },
    { path: 'exchange-rates', loadComponent: () => import('./features/admin/exchange-rates/exchange-rates.component').then(m => m.ExchangeRatesComponent) },
    { path: 'corridors', loadComponent: () => import('./features/admin/corridors/corridors.component').then(m => m.CorridorsComponent) },
    { path: 'fee-grids', loadComponent: () => import('./features/admin/fee-grids/fee-grids.component').then(m => m.FeeGridsComponent) },
    { path: 'compliance', loadComponent: () => import('./features/admin/compliance/compliance.component').then(m => m.ComplianceComponent) },
    { path: 'reports', loadComponent: () => import('./features/admin/reports/reports.component').then(m => m.ReportsComponent) },
    { path: 'audit', loadComponent: () => import('./features/admin/audit/audit.component').then(m => m.AuditComponent) }
  ]},
  { path: 'manager', component: SecuredLayoutComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_MANAGER'] }, children: [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: 'dashboard', loadComponent: () => import('./features/manager/dashboard/manager-dashboard.component').then(m => m.ManagerDashboardComponent) },
    { path: 'agents', loadComponent: () => import('./features/manager/agents/agents.component').then(m => m.AgentsComponent) },
    { path: 'cash-registers', loadComponent: () => import('./features/manager/cash-registers/cash-registers.component').then(m => m.CashRegistersComponent) },
    { path: 'reports', loadComponent: () => import('./features/manager/reports/manager-reports.component').then(m => m.ManagerReportsComponent) }
  ]},
  { path: 'agent', component: SecuredLayoutComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_AGENT'] }, children: [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: 'dashboard', loadComponent: () => import('./features/agent/dashboard/agent-dashboard.component').then(m => m.AgentDashboardComponent) },
    { path: 'transfers', loadComponent: () => import('./features/agent/transfers/transfers.component').then(m => m.TransfersComponent) },
    { path: 'create-transfer', loadComponent: () => import('./features/agent/create-transfer/create-transfer.component').then(m => m.CreateTransferComponent) },
    { path: 'fee-simulation', loadComponent: () => import('./features/agent/fee-simulation/fee-simulation.component').then(m => m.FeeSimulationComponent) },
    { path: 'payout', loadComponent: () => import('./features/agent/payout/payout.component').then(m => m.PayoutComponent) },
    { path: 'cash-register', loadComponent: () => import('./features/agent/cash-register/cash-register.component').then(m => m.CashRegisterComponent) }
  ]},
  { path: 'client', component: SecuredLayoutComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_CLIENT'] }, children: [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: 'dashboard', loadComponent: () => import('./features/client/dashboard/client-dashboard.component').then(m => m.ClientDashboardComponent) },
    { path: 'profile', loadComponent: () => import('./features/client/profile/profile.component').then(m => m.ProfileComponent) },
    { path: 'beneficiaries', loadComponent: () => import('./features/client/beneficiaries/beneficiaries.component').then(m => m.BeneficiariesComponent) },
    { path: 'transfer-tracking', loadComponent: () => import('./features/client/transfer-tracking/transfer-tracking.component').then(m => m.TransferTrackingComponent) }
  ]},
  { path: 'mobile-money', component: SecuredLayoutComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_AGENT'] }, children: [
    { path: '', loadComponent: () => import('./features/mobile-money/mobile-money.component').then(m => m.MobileMoneyComponent) }
  ]},
  { path: 'chatbot', component: SecuredLayoutComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_AGENT', 'ROLE_CLIENT'] }, children: [
    { path: '', loadComponent: () => import('./features/chatbot/chatbot.component').then(m => m.ChatbotComponent) }
  ]},
  { path: 'notifications', component: SecuredLayoutComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_AGENT', 'ROLE_CLIENT'] }, children: [
    { path: '', loadComponent: () => import('./features/notifications/notifications.component').then(m => m.NotificationsComponent) }
  ]},
  { path: '**', redirectTo: 'auth/login' }
];
