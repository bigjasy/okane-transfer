import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CorridorResponse, FeeGridRequest, FeeGridResponse } from '../../../core/models/agency.models';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-feegridscomponent',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page fee-grids-page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('admin.fee_grids.title') }}</h1>
      <p class="page-subtitle">{{ t('admin.fee_grids.subtitle') }}</p>
    </div>
    <div class="header-actions">
      <button class="btn" type="button" (click)="exportCsv()" [disabled]="exporting">{{ t('admin.fee_grids.export_csv') }}</button>
      <button class="btn primary" type="button" (click)="startCreate()">{{ t('admin.fee_grids.create') }}</button>
    </div>
  </div>

  @if (errorMessage) { <div class="alert error">{{ errorMessage }}</div> }
  @if (successMessage) { <div class="alert success">{{ successMessage }}</div> }

  @if (showForm) {
    <div class="card form-card">
      <h3>{{ editing ? t('admin.fee_grids.edit_title') : t('admin.fee_grids.create_title') }}</h3>
      <p class="hint">{{ t('admin.fee_grids.commission_hint') }}</p>
      <form class="form-grid" (ngSubmit)="save()">
        <label class="field">{{ t('admin.fee_grids.corridor') }}
          <select [(ngModel)]="form.corridorId" name="corridor" required>
            @for (c of corridors; track c.id) {
              <option [ngValue]="c.id">{{ corridorLabel(c) }}</option>
            }
          </select>
        </label>
        <label class="field">{{ t('admin.fee_grids.min') }}
          <input type="number" [(ngModel)]="form.minAmount" name="min" required min="0" step="0.01">
        </label>
        <label class="field">{{ t('admin.fee_grids.max') }}
          <input type="number" [(ngModel)]="form.maxAmount" name="max" required min="0" step="0.01">
        </label>
        <label class="field">{{ t('admin.fee_grids.fixed') }}
          <input type="number" [(ngModel)]="form.fixedFee" name="fixed" required min="0" step="0.01">
        </label>
        <label class="field">{{ t('admin.fee_grids.percentage') }}
          <input type="number" [(ngModel)]="form.percentageFee" name="pct" required min="0" step="0.01">
        </label>
        <label class="field">{{ t('admin.fee_grids.agency_share') }}
          <input type="number" [(ngModel)]="form.agencyCommissionRate" name="agency" required min="0" max="100" step="0.01">
        </label>
        <label class="field">{{ t('admin.fee_grids.central_share') }}
          <input type="number" [(ngModel)]="form.centralCommissionRate" name="central" required min="0" max="100" step="0.01">
        </label>
        <label class="field">{{ t('admin.fee_grids.valid_from') }}
          <input type="date" [(ngModel)]="form.validFrom" name="from">
        </label>
        <label class="field">{{ t('admin.fee_grids.valid_to') }}
          <input type="date" [(ngModel)]="form.validTo" name="to">
        </label>
        <label class="field checkbox">
          <input type="checkbox" [(ngModel)]="form.active" name="active"> {{ t('admin.fee_grids.active') }}
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
      <p class="state">{{ t('admin.fee_grids.loading') }}</p>
    } @else if (items.length === 0) {
      <p class="state">{{ t('admin.fee_grids.empty') }}</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>{{ t('admin.fee_grids.id') }}</th>
            <th>{{ t('admin.fee_grids.corridor') }}</th>
            <th>{{ t('admin.fee_grids.min') }}</th>
            <th>{{ t('admin.fee_grids.max') }}</th>
            <th>{{ t('admin.fee_grids.fee_type') }}</th>
            <th>{{ t('admin.fee_grids.fee_value') }}</th>
            <th>{{ t('admin.fee_grids.active') }}</th>
            <th>{{ t('admin.fee_grids.action') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (item of items; track item.id) {
            <tr>
              <td>#{{ item.id }}</td>
              <td>{{ corridorLabelById(item.corridorId) }}</td>
              <td>{{ formatAmount(item.minAmount) }}</td>
              <td>{{ formatAmount(item.maxAmount) }}</td>
              <td>{{ feeTypeLabel(item) }}</td>
              <td>{{ feeValueLabel(item) }}</td>
              <td><app-status-badge [value]="item.active ? 'ACTIVE' : 'DISABLED'"/></td>
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

  <div class="pagination">
    <button type="button" (click)="previousPage()" [disabled]="loading || page === 0">{{ t('admin.fee_grids.previous') }}</button>
    <span>{{ t('admin.fee_grids.page') }} {{ page + 1 }} {{ t('admin.fee_grids.of') }} {{ displayTotalPages }}</span>
    <button type="button" (click)="nextPage()" [disabled]="loading || !hasNextPage">{{ t('admin.fee_grids.next') }}</button>
    <span class="muted">{{ totalElements }} {{ t('admin.fee_grids.total') }}</span>
  </div>
</section>`,
  styles: [`
    .fee-grids-page { display: grid; gap: 16px; }
    .header-actions { display: flex; gap: .5rem; flex-wrap: wrap; }
    .alert { border-radius: 6px; padding: 12px 14px; font-weight: 600; }
    .success { background: #dcfce7; color: #166534; }
    .error { background: #fef2f2; border: 1px solid #fecaca; color: #991b1b; }
    .form-card { margin-bottom: .5rem; }
    .form-grid { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); }
    .field { display: grid; gap: .35rem; font-weight: 600; color: #334155; }
    .field select, .field input { font: inherit; padding: .55rem .75rem; border: 1px solid #cbd5e1; border-radius: .5rem; }
    .checkbox { align-items: center; grid-auto-flow: column; justify-content: start; }
    .form-actions { display: flex; gap: .5rem; grid-column: 1 / -1; }
    .actions { display: flex; gap: .35rem; flex-wrap: wrap; }
    .hint { color: #64748b; margin: 0 0 .75rem; }
    .state { color: #475569; margin: 0; padding: 16px; }
    .pagination { align-items: center; display: flex; flex-wrap: wrap; gap: 12px; justify-content: flex-end; }
    .pagination button { background: #fff; border: 1px solid #cbd5e1; border-radius: 6px; cursor: pointer; font: inherit; padding: 8px 12px; }
    .pagination button:disabled { cursor: not-allowed; opacity: .5; }
    .muted { color: #64748b; }
  `]
})
export class FeeGridsComponent implements OnInit {
  items: FeeGridResponse[] = [];
  corridors: CorridorResponse[] = [];
  form: FeeGridRequest = this.emptyForm();
  editing: FeeGridResponse | null = null;
  showForm = false;
  loading = false;
  saving = false;
  exporting = false;
  actionId: number | null = null;
  errorMessage = '';
  successMessage = '';
  page = 0;
  size = 20;
  totalElements = 0;
  totalPages = 0;

  constructor(private readonly data: DataService, private readonly i18n: I18nService) {}

  ngOnInit(): void {
    this.data.corridors().subscribe(c => this.corridors = c.filter(x => x.active));
    this.loadFeeGrids();
  }

  get hasNextPage(): boolean { return this.totalPages > 0 && this.page + 1 < this.totalPages; }
  get displayTotalPages(): number { return Math.max(this.totalPages, 1); }

  loadFeeGrids(page = this.page): void {
    this.loading = true;
    this.errorMessage = '';
    this.data.feeGrids(page, this.size).subscribe({
      next: result => {
        this.items = result.feeGrids;
        this.page = result.page;
        this.size = result.size;
        this.totalElements = result.totalElements;
        this.totalPages = result.totalPages;
        this.loading = false;
      },
      error: () => {
        this.items = [];
        this.errorMessage = this.t('admin.fee_grids.load_error');
        this.loading = false;
      }
    });
  }

  startCreate(): void {
    this.editing = null;
    this.form = this.emptyForm();
    this.showForm = true;
  }

  startEdit(item: FeeGridResponse): void {
    this.editing = item;
    this.form = {
      corridorId: item.corridorId ?? 0,
      minAmount: item.minAmount,
      maxAmount: item.maxAmount,
      fixedFee: item.fixedFee,
      percentageFee: item.percentageFee,
      agencyCommissionRate: item.agencyCommissionRate,
      centralCommissionRate: item.centralCommissionRate,
      validFrom: item.validFrom?.slice(0, 10),
      validTo: item.validTo?.slice(0, 10),
      active: item.active
    };
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editing = null;
  }

  save(): void {
    if (Math.abs(this.form.agencyCommissionRate + this.form.centralCommissionRate - 100) > 0.01) {
      this.errorMessage = this.t('admin.fee_grids.commission_hint');
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    const req = this.editing
      ? this.data.updateFeeGrid(this.editing.id, this.form)
      : this.data.createFeeGrid(this.form);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = this.editing ? this.t('admin.fee_grids.update_success') : this.t('admin.fee_grids.create_success');
        this.showForm = false;
        this.editing = null;
        this.loadFeeGrids();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || this.t('admin.fee_grids.save_error');
      }
    });
  }

  toggle(item: FeeGridResponse): void {
    this.actionId = item.id;
    this.data.toggleFeeGridActivation(item.id, !item.active).subscribe({
      next: () => {
        this.actionId = null;
        this.successMessage = this.t('admin.fee_grids.status_success');
        this.loadFeeGrids();
      },
      error: (err) => {
        this.actionId = null;
        this.errorMessage = err?.error?.message || this.t('admin.fee_grids.status_error');
      }
    });
  }

  exportCsv(): void {
    this.exporting = true;
    this.data.exportFeeGridsCsv().subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'fee-grids.csv';
        a.click();
        URL.revokeObjectURL(url);
        this.exporting = false;
        this.successMessage = this.t('admin.fee_grids.export_success');
      },
      error: () => {
        this.exporting = false;
        this.errorMessage = this.t('admin.fee_grids.export_error');
      }
    });
  }

  previousPage(): void {
    if (this.page === 0) return;
    this.loadFeeGrids(this.page - 1);
  }

  nextPage(): void {
    if (!this.hasNextPage) return;
    this.loadFeeGrids(this.page + 1);
  }

  corridorLabel(c: CorridorResponse): string {
    return `${c.sourceCountry?.name ?? '?'} → ${c.destinationCountry?.name ?? '?'}`;
  }

  corridorLabelById(id?: number): string {
    const c = this.corridors.find(x => x.id === id);
    return c ? this.corridorLabel(c) : (id ? `#${id}` : this.t('admin.fee_grids.not_available'));
  }

  feeTypeLabel(item: FeeGridResponse): string {
    const hasFixed = item.fixedFee > 0;
    const hasPercentage = item.percentageFee > 0;
    if (hasFixed && hasPercentage) return this.t('admin.fee_grids.mixed_fee');
    if (hasFixed) return this.t('admin.fee_grids.fixed_fee');
    if (hasPercentage) return this.t('admin.fee_grids.percentage_fee');
    return this.t('admin.fee_grids.not_available');
  }

  feeValueLabel(item: FeeGridResponse): string {
    const values: string[] = [];
    if (item.fixedFee > 0) values.push(this.formatAmount(item.fixedFee));
    if (item.percentageFee > 0) values.push(`${this.formatAmount(item.percentageFee)}%`);
    return values.length ? values.join(' + ') : this.t('admin.fee_grids.not_available');
  }

  emptyForm(): FeeGridRequest {
    const today = new Date().toISOString().slice(0, 10);
    return {
      corridorId: this.corridors[0]?.id ?? 1,
      minAmount: 100,
      maxAmount: 5000,
      fixedFee: 25,
      percentageFee: 1.5,
      agencyCommissionRate: 40,
      centralCommissionRate: 60,
      validFrom: today,
      validTo: undefined,
      active: true
    };
  }

  formatAmount(value: number): string {
    return new Intl.NumberFormat(undefined, { maximumFractionDigits: 2 }).format(value);
  }

  t(key: string): string { return this.i18n.get(key); }
}
