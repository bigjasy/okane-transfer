import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { CorridorRequest, CorridorResponse } from '../../../core/models/agency.models';
import { CountryResponse } from '../../../core/models/referential.models';

@Component({
  selector: 'app-corridors',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('admin.corridors.title') }}</h1>
      <p class="page-subtitle">{{ t('admin.corridors.subtitle') }}</p>
    </div>
    <div class="header-actions">
      <button class="btn" type="button" (click)="load()" [disabled]="loading">{{ t('admin.corridors.refresh') }}</button>
      <button class="btn primary" type="button" (click)="startCreate()">{{ t('admin.corridors.create') }}</button>
    </div>
  </div>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (successMsg) { <div class="alert success">{{ successMsg }}</div> }

  @if (showForm) {
    <div class="card form-card">
      <h3>{{ editing ? t('admin.corridors.edit_title') : t('admin.corridors.create_title') }}</h3>
      <form class="form-grid" (ngSubmit)="save()">
        <label class="field">{{ t('admin.corridors.source') }}
          <select [(ngModel)]="form.sourceCountryId" name="source" required [disabled]="!!editing">
            @for (c of countries; track c.id) {
              <option [ngValue]="c.id">{{ c.name }} ({{ c.isoCode }})</option>
            }
          </select>
        </label>
        <label class="field">{{ t('admin.corridors.destination') }}
          <select [(ngModel)]="form.destinationCountryId" name="dest" required [disabled]="!!editing">
            @for (c of countries; track c.id) {
              <option [ngValue]="c.id">{{ c.name }} ({{ c.isoCode }})</option>
            }
          </select>
        </label>
        <label class="field">{{ t('admin.corridors.daily_limit') }}
          <input type="number" [(ngModel)]="form.dailyLimit" name="daily" required min="1">
        </label>
        <label class="field">{{ t('admin.corridors.monthly_limit') }}
          <input type="number" [(ngModel)]="form.monthlyLimit" name="monthly" required min="1">
        </label>
        <label class="field checkbox">
          <input type="checkbox" [(ngModel)]="form.active" name="active"> {{ t('admin.corridors.active') }}
        </label>
        <div class="form-actions">
          <button class="btn primary" type="submit" [disabled]="saving">{{ t('common.save') }}</button>
          <button class="btn" type="button" (click)="cancelForm()">{{ t('common.cancel') }}</button>
        </div>
      </form>
    </div>
  }

  <div class="card table-wrap">
    @if (loading) {
      <p class="muted">{{ t('admin.corridors.loading') }}</p>
    } @else if (items.length === 0) {
      <p class="muted">{{ t('admin.corridors.empty') }}</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>{{ t('admin.corridors.source') }}</th>
            <th>{{ t('admin.corridors.destination') }}</th>
            <th>{{ t('admin.corridors.active') }}</th>
            <th>{{ t('admin.corridors.daily_limit') }}</th>
            <th>{{ t('admin.corridors.monthly_limit') }}</th>
            <th>{{ t('admin.corridors.action') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (item of items; track item.id) {
            <tr>
              <td>{{ item.sourceCountry?.name }}</td>
              <td>{{ item.destinationCountry?.name }}</td>
              <td><app-status-badge [value]="item.active ? 'ACTIVE' : 'DISABLED'"/></td>
              <td>{{ formatAmount(item.dailyLimit) }}</td>
              <td>{{ formatAmount(item.monthlyLimit) }}</td>
              <td class="actions">
                <button class="btn" type="button" (click)="startEdit(item)">{{ t('common.edit') }}</button>
                <button class="btn" type="button" (click)="toggle(item)" [disabled]="actionId === item.id">
                  {{ item.active ? t('common.suspend') : t('common.activate') }}
                </button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  </div>
</section>`,
  styles: [`
    .header-actions { display: flex; gap: .5rem; flex-wrap: wrap; }
    .alert { padding: .75rem 1rem; border-radius: .75rem; margin-bottom: 1rem; }
    .alert.danger { background: #fee2e2; color: #991b1b; }
    .alert.success { background: #dcfce7; color: #166534; }
    .form-card { margin-bottom: 1rem; }
    .form-grid { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); }
    .field { display: grid; gap: .35rem; font-weight: 600; color: #334155; }
    .field select, .field input { font: inherit; padding: .55rem .75rem; border: 1px solid #cbd5e1; border-radius: .5rem; }
    .checkbox { align-items: center; grid-auto-flow: column; justify-content: start; }
    .form-actions { display: flex; gap: .5rem; grid-column: 1 / -1; }
    .actions { display: flex; gap: .35rem; flex-wrap: wrap; }
    .muted { color: #64748b; }
  `]
})
export class CorridorsComponent implements OnInit {
  items: CorridorResponse[] = [];
  countries: CountryResponse[] = [];
  form: CorridorRequest = this.emptyForm();
  editing: CorridorResponse | null = null;
  showForm = false;
  loading = false;
  saving = false;
  actionId: number | null = null;
  errorMsg = '';
  successMsg = '';

  constructor(private data: DataService, private i18n: I18nService) {}

  ngOnInit(): void {
    this.data.countries().subscribe(c => this.countries = c.filter(x => x.active));
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.data.corridors().subscribe({
      next: items => { this.items = items; this.loading = false; },
      error: err => {
        this.loading = false;
        this.errorMsg = `${this.t('admin.corridors.load_error')} (${err?.status ?? '?'})`;
      }
    });
  }

  startCreate(): void {
    this.editing = null;
    this.form = this.emptyForm();
    this.showForm = true;
  }

  startEdit(item: CorridorResponse): void {
    this.editing = item;
    this.form = {
      sourceCountryId: item.sourceCountry.id,
      destinationCountryId: item.destinationCountry.id,
      dailyLimit: item.dailyLimit,
      monthlyLimit: item.monthlyLimit,
      active: item.active
    };
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editing = null;
  }

  save(): void {
    if (this.form.sourceCountryId === this.form.destinationCountryId) {
      this.errorMsg = this.t('admin.corridors.same_country_error');
      return;
    }
    this.saving = true;
    this.errorMsg = '';
    this.successMsg = '';
    const req = this.editing
      ? this.data.updateCorridor(this.editing.id, this.form)
      : this.data.createCorridor(this.form);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.successMsg = this.editing ? this.t('admin.corridors.update_success') : this.t('admin.corridors.create_success');
        this.showForm = false;
        this.editing = null;
        this.load();
      },
      error: err => {
        this.saving = false;
        this.errorMsg = err?.error?.message || this.t('admin.corridors.save_error');
      }
    });
  }

  toggle(item: CorridorResponse): void {
    this.actionId = item.id;
    this.data.toggleCorridorActivation(item.id, !item.active).subscribe({
      next: () => {
        this.actionId = null;
        this.successMsg = this.t('admin.corridors.status_success');
        this.load();
      },
      error: err => {
        this.actionId = null;
        this.errorMsg = err?.error?.message || this.t('admin.corridors.status_error');
      }
    });
  }

  emptyForm(): CorridorRequest {
    return { sourceCountryId: 1, destinationCountryId: 2, dailyLimit: 100000, monthlyLimit: 1000000, active: true };
  }

  formatAmount(value: number): string {
    return new Intl.NumberFormat(undefined, { maximumFractionDigits: 0 }).format(value);
  }

  t(key: string): string { return this.i18n.get(key); }
}
