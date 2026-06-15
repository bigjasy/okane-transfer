import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { AgencyRequest, AgencyResponse, AgencyStaffResponse } from '../../../core/models/agency.models';
import { CountryResponse } from '../../../core/models/referential.models';
import { UserSummaryResponse } from '../../../core/models/user.models';
import { AgencyStatus } from '../../../core/models/enums';

@Component({
  selector: 'app-agencies',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('admin.agencies.title') }}</h1>
      <p class="page-subtitle">{{ t('admin.agencies.subtitle') }}</p>
    </div>
    <button class="btn" type="button" (click)="load()" [disabled]="loading">{{ t('admin.agencies.refresh') }}</button>
  </div>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (successMsg) { <div class="alert success">{{ successMsg }}</div> }

  <div class="grid cols-2">
    <div class="card">
      <h3>{{ editing ? t('admin.agencies.edit_title') : t('admin.agencies.create_title') }}</h3>
      <form class="form-grid" (ngSubmit)="saveAgency()">
        <input name="code" [placeholder]="t('admin.agencies.code')" [(ngModel)]="form.code" required [readonly]="!!editing">
        <input name="name" [placeholder]="t('admin.agencies.name')" [(ngModel)]="form.name" required>
        <input name="city" [placeholder]="t('admin.agencies.city')" [(ngModel)]="form.city" required>
        <input name="address" [placeholder]="t('admin.agencies.address')" [(ngModel)]="form.address" required>
        <select name="countryId" [(ngModel)]="form.countryId" required>
          @for (c of countries; track c.id) {
            <option [ngValue]="c.id">{{ c.name }} ({{ c.isoCode }})</option>
          }
        </select>
        <input name="dailyLimit" type="number" [placeholder]="t('admin.agencies.limit')" [(ngModel)]="form.dailyLimit" required>
        <button class="btn primary" type="submit" [disabled]="creating">{{ t(editing ? 'common.save' : 'common.create') }}</button>
        @if (editing) {
          <button class="btn" type="button" (click)="cancelEdit()">{{ t('common.cancel') }}</button>
        }
      </form>
    </div>

    @if (staff) {
      <div class="card">
        <h3>{{ t('admin.agencies.staff_title') }} — {{ staff.agencyName }}</h3>
        <div class="staff-block">
          <h4>{{ t('admin.agencies.managers') }}</h4>
          @if (staff.managers.length === 0) { <p class="muted">{{ t('admin.agencies.no_staff') }}</p> }
          <ul>@for (m of staff.managers; track m.id) { <li>{{ m.fullName }} — {{ m.email }}</li> }</ul>
        </div>
        <div class="staff-block">
          <h4>{{ t('admin.agencies.agents') }}</h4>
          @if (staff.agents.length === 0) { <p class="muted">{{ t('admin.agencies.no_staff') }}</p> }
          <ul>@for (a of staff.agents; track a.id) { <li>{{ a.fullName }} — {{ a.email }}</li> }</ul>
        </div>
        <div class="assign-row">
          <select [(ngModel)]="selectedAgentId" name="agentPick">
            <option [ngValue]="null">{{ t('admin.agencies.select_agent') }}</option>
            @for (u of availableAgents; track u.id) {
              <option [ngValue]="u.id">{{ u.fullName }} ({{ u.email }})</option>
            }
          </select>
          <button class="btn" type="button" (click)="assignAgent()" [disabled]="!selectedAgentId">{{ t('admin.agencies.assign_agent') }}</button>
        </div>
        <div class="assign-row">
          <select [(ngModel)]="selectedManagerId" name="managerPick">
            <option [ngValue]="null">{{ t('admin.agencies.select_manager') }}</option>
            @for (u of availableManagers; track u.id) {
              <option [ngValue]="u.id">{{ u.fullName }} ({{ u.email }})</option>
            }
          </select>
          <button class="btn" type="button" (click)="assignManager()" [disabled]="!selectedManagerId">{{ t('admin.agencies.assign_manager') }}</button>
        </div>
      </div>
    }
  </div>

  <div class="card table-wrap">
    @if (loading) {
      <p class="muted">{{ t('admin.agencies.loading') }}</p>
    } @else if (items.length === 0) {
      <p class="muted">{{ t('admin.agencies.empty') }}</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>{{ t('admin.agencies.code') }}</th>
            <th>{{ t('admin.agencies.name') }}</th>
            <th>{{ t('admin.agencies.city') }}</th>
            <th>{{ t('admin.agencies.country') }}</th>
            <th>{{ t('admin.agencies.status') }}</th>
            <th>{{ t('admin.agencies.limit') }}</th>
            <th>{{ t('admin.agencies.action') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (item of items; track item.id) {
            <tr [class.selected]="selectedAgencyId === item.id">
              <td>{{ item.code }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.city }}</td>
              <td>{{ item.country }}</td>
              <td><app-status-badge [value]="item.status"/></td>
              <td>{{ item.dailyLimit }}</td>
              <td class="actions">
                <button class="btn" type="button" (click)="selectAgency(item)">{{ t('admin.agencies.staff_title') }}</button>
                <button class="btn" type="button" (click)="startEdit(item)">{{ t('common.edit') }}</button>
                <button class="btn" type="button" (click)="toggleStatus(item)" [disabled]="actionId === item.id">
                  {{ item.status === 'ACTIVE' ? t('common.suspend') : t('common.activate') }}
                </button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  </div>

  <div class="pagination">
    <button type="button" (click)="previousPage()" [disabled]="loading || page === 0">{{ t('admin.agencies.previous') }}</button>
    <span>{{ t('admin.agencies.page') }} {{ page + 1 }} / {{ totalPages || 1 }}</span>
    <button type="button" (click)="nextPage()" [disabled]="loading || page + 1 >= totalPages">{{ t('admin.agencies.next') }}</button>
  </div>
</section>`,
  styles: [`
    .alert { padding: .75rem 1rem; border-radius: .75rem; margin-bottom: 1rem; }
    .alert.danger { background: #fee2e2; color: #991b1b; }
    .alert.success { background: #dcfce7; color: #166534; }
    .form-grid { display: grid; gap: .75rem; }
    .form-grid input, .form-grid select { padding: .55rem .75rem; border: 1px solid #cbd5e1; border-radius: .5rem; }
    .staff-block { margin-bottom: 1rem; }
    .staff-block ul { margin: .25rem 0 0; padding-left: 1.25rem; }
    .assign-row { display: flex; gap: .5rem; margin-top: .75rem; flex-wrap: wrap; }
    .assign-row select { flex: 1; min-width: 200px; padding: .5rem; }
    .actions { display: flex; gap: .35rem; flex-wrap: wrap; }
    tr.selected { background: #eff6ff; }
    .muted { color: #64748b; }
    .pagination { display: flex; gap: 1rem; align-items: center; margin-top: 1rem; }
  `]
})
export class AgenciesComponent implements OnInit {
  items: AgencyResponse[] = [];
  countries: CountryResponse[] = [];
  staff: AgencyStaffResponse | null = null;
  availableAgents: UserSummaryResponse[] = [];
  availableManagers: UserSummaryResponse[] = [];
  form: AgencyRequest = { code: '', name: '', address: '', city: '', countryId: 1, dailyLimit: 100000 };
  editing: AgencyResponse | null = null;
  selectedAgencyId: number | null = null;
  selectedAgentId: number | null = null;
  selectedManagerId: number | null = null;
  page = 0;
  size = 20;
  totalPages = 0;
  loading = false;
  creating = false;
  actionId: number | null = null;
  errorMsg = '';
  successMsg = '';

  constructor(private data: DataService, private i18n: I18nService) {}

  ngOnInit(): void {
    this.data.countries().subscribe(c => {
      this.countries = c.filter(x => x.active);
      if (this.countries.length) this.form.countryId = this.countries[0].id;
    });
    this.loadUsers();
    this.load();
  }

  loadUsers(): void {
    this.data.users(0, 200).subscribe(r => {
      this.availableAgents = r.content.filter(u => u.role === 'ROLE_AGENT');
      this.availableManagers = r.content.filter(u => u.role === 'ROLE_MANAGER');
    });
  }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.data.agencies(this.page, this.size).subscribe({
      next: result => {
        this.items = result.content;
        this.totalPages = result.totalPages;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMsg = `${this.t('admin.agencies.load_error')} (${err?.status ?? '?'})`;
      }
    });
  }

  saveAgency(): void {
    this.creating = true;
    this.errorMsg = '';
    this.successMsg = '';
    const req = this.editing
      ? this.data.updateAgency(this.editing.id, this.form)
      : this.data.createAgency(this.form);
    req.subscribe({
      next: () => {
        this.creating = false;
        this.successMsg = this.editing ? this.t('admin.agencies.update_success') : this.t('admin.agencies.create_success');
        this.cancelEdit();
        this.load();
      },
      error: err => {
        this.creating = false;
        this.errorMsg = err?.error?.message || this.t('admin.agencies.create_error');
      }
    });
  }

  startEdit(item: AgencyResponse): void {
    this.editing = item;
    this.data.agencyById(item.id).subscribe({
      next: full => {
        this.form = {
          code: full.code,
          name: full.name,
          address: full.address ?? '',
          city: full.city,
          countryId: this.countries.find(c => c.name === full.country)?.id ?? this.form.countryId,
          dailyLimit: full.dailyLimit
        };
      },
      error: () => {
        this.form = {
          code: item.code,
          name: item.name,
          address: item.address ?? '',
          city: item.city,
          countryId: this.countries.find(c => c.name === item.country)?.id ?? this.form.countryId,
          dailyLimit: item.dailyLimit
        };
      }
    });
  }

  cancelEdit(): void {
    this.editing = null;
    this.form = { code: '', name: '', address: '', city: '', countryId: this.countries[0]?.id ?? 1, dailyLimit: 100000 };
  }

  selectAgency(item: AgencyResponse): void {
    this.selectedAgencyId = item.id;
    this.data.agencyStaff(item.id).subscribe({
      next: s => this.staff = s,
      error: () => this.errorMsg = this.t('admin.agencies.load_error')
    });
  }

  assignAgent(): void {
    if (!this.selectedAgencyId || !this.selectedAgentId) return;
    this.data.assignAgentToAgency(this.selectedAgencyId, this.selectedAgentId).subscribe({
      next: () => {
        this.successMsg = this.t('admin.agencies.assign_success');
        this.selectAgency(this.items.find(a => a.id === this.selectedAgencyId)!);
        this.selectedAgentId = null;
      },
      error: err => this.errorMsg = err?.error?.message || this.t('admin.agencies.assign_error')
    });
  }

  assignManager(): void {
    if (!this.selectedAgencyId || !this.selectedManagerId) return;
    this.data.assignManagerToAgency(this.selectedAgencyId, this.selectedManagerId).subscribe({
      next: () => {
        this.successMsg = this.t('admin.agencies.assign_success');
        this.selectAgency(this.items.find(a => a.id === this.selectedAgencyId)!);
        this.selectedManagerId = null;
      },
      error: err => this.errorMsg = err?.error?.message || this.t('admin.agencies.assign_error')
    });
  }

  toggleStatus(item: AgencyResponse): void {
    const next: AgencyStatus = item.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    this.actionId = item.id;
    this.data.updateAgencyStatus(item.id, next).subscribe({
      next: () => {
        this.actionId = null;
        this.successMsg = this.t('admin.agencies.status_success');
        this.load();
      },
      error: err => {
        this.actionId = null;
        this.errorMsg = err?.error?.message || this.t('admin.agencies.status_error');
      }
    });
  }

  previousPage(): void { if (this.page > 0) { this.page--; this.load(); } }
  nextPage(): void { if (this.page + 1 < this.totalPages) { this.page++; this.load(); } }
  t(key: string): string { return this.i18n.get(key); }
}
