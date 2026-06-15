import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { UserCreateRequest, UserSummaryResponse } from '../../../core/models/user.models';
import { ROLES, UserStatus } from '../../../core/models/enums';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('admin.users.title') }}</h1>
      <p class="page-subtitle">{{ t('admin.users.subtitle') }}</p>
    </div>
  </div>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (successMsg) { <div class="alert success">{{ successMsg }}</div> }

  <div class="card">
    <h3>{{ t('admin.users.create_internal_user') }}</h3>
    <form class="form-grid" (ngSubmit)="create()">
      <input name="fn" [placeholder]="t('admin.users.first_name')" [(ngModel)]="form.firstName" required>
      <input name="ln" [placeholder]="t('admin.users.last_name')" [(ngModel)]="form.lastName" required>
      <input name="email" type="email" [placeholder]="t('admin.users.email')" [(ngModel)]="form.email" required>
      <input name="tel" [placeholder]="t('admin.users.phone')" [(ngModel)]="form.phoneNumber" required>
      <select name="role" [(ngModel)]="form.role">
        @for (r of roles; track r) { <option [value]="r">{{ r }}</option> }
      </select>
      <input name="agency" type="number" [placeholder]="t('admin.users.agency_id')" [(ngModel)]="form.agencyId">
      <button class="btn primary" type="submit" [disabled]="creating">{{ t('common.create') }}</button>
    </form>
  </div>

  <div class="card table-wrap">
    @if (loading) {
      <p class="muted">{{ t('admin.users.loading') }}</p>
    } @else if (users.length === 0) {
      <p class="muted">{{ t('admin.users.empty') }}</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>{{ t('admin.users.id') }}</th>
            <th>{{ t('admin.users.name') }}</th>
            <th>{{ t('admin.users.email') }}</th>
            <th>{{ t('admin.users.role') }}</th>
            <th>{{ t('admin.users.status') }}</th>
            <th>{{ t('admin.users.agency') }}</th>
            <th>{{ t('admin.users.action') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (u of users; track u.id) {
            <tr>
              <td>{{ u.id }}</td>
              <td>{{ u.fullName }}</td>
              <td>{{ u.email }}</td>
              <td>{{ u.role }}</td>
              <td><app-status-badge [value]="u.status"/></td>
              <td>{{ u.agencyName || '—' }}</td>
              <td>
                <button class="btn" type="button" (click)="toggle(u)" [disabled]="actionId === u.id">
                  {{ t('common.activate_suspend') }}
                </button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  </div>

  <div class="pagination">
    <button type="button" (click)="previousPage()" [disabled]="loading || page === 0">{{ t('admin.users.previous') }}</button>
    <span>{{ t('admin.users.page') }} {{ page + 1 }} / {{ totalPages || 1 }}</span>
    <button type="button" (click)="nextPage()" [disabled]="loading || page + 1 >= totalPages">{{ t('admin.users.next') }}</button>
  </div>
</section>`,
  styles: [`
    .alert { padding: .75rem 1rem; border-radius: .75rem; margin-bottom: 1rem; }
    .alert.danger { background: #fee2e2; color: #991b1b; }
    .alert.success { background: #dcfce7; color: #166534; }
    .muted { color: #64748b; }
    .pagination { display: flex; gap: 1rem; align-items: center; margin-top: 1rem; }
  `]
})
export class UsersComponent implements OnInit {
  roles = ROLES.filter(r => r !== 'ROLE_CLIENT');
  users: UserSummaryResponse[] = [];
  form: UserCreateRequest = { firstName: '', lastName: '', email: '', phoneNumber: '', role: 'ROLE_AGENT', agencyId: 1 };
  page = 0;
  size = 20;
  totalPages = 0;
  loading = false;
  creating = false;
  actionId: number | null = null;
  errorMsg = '';
  successMsg = '';

  constructor(private data: DataService, private i18n: I18nService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.data.users(this.page, this.size).subscribe({
      next: result => {
        this.users = result.content;
        this.totalPages = result.totalPages;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMsg = `${this.t('admin.users.load_error')} (${err?.status ?? '?'})`;
      }
    });
  }

  create(): void {
    this.creating = true;
    this.errorMsg = '';
    this.successMsg = '';
    this.data.createUser(this.form).subscribe({
      next: () => {
        this.creating = false;
        this.successMsg = this.t('admin.users.create_success');
        this.form = { firstName: '', lastName: '', email: '', phoneNumber: '', role: 'ROLE_AGENT', agencyId: 1 };
        this.load();
      },
      error: err => {
        this.creating = false;
        this.errorMsg = err?.error?.message || this.t('admin.users.create_error');
      }
    });
  }

  toggle(u: UserSummaryResponse): void {
    const status: UserStatus = u.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    this.actionId = u.id;
    this.data.updateUserStatus(u.id, status).subscribe({
      next: () => {
        this.actionId = null;
        this.successMsg = this.t('admin.users.status_success');
        this.load();
      },
      error: err => {
        this.actionId = null;
        this.errorMsg = err?.error?.message || this.t('admin.users.status_error');
      }
    });
  }

  previousPage(): void {
    if (this.page === 0) return;
    this.page--;
    this.load();
  }

  nextPage(): void {
    if (this.page + 1 >= this.totalPages) return;
    this.page++;
    this.load();
  }

  t(key: string): string { return this.i18n.get(key); }
}
