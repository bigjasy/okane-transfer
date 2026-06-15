import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { CurrencyResponse } from '../../../core/models/referential.models';

@Component({
  selector: 'app-currencies',
  standalone: true,
  imports: [CommonModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('admin.currencies.title') }}</h1>
      <p class="page-subtitle">{{ t('admin.currencies.subtitle') }}</p>
    </div>
    <button class="btn" type="button" (click)="load()" [disabled]="loading">{{ t('admin.currencies.refresh') }}</button>
  </div>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (successMsg) { <div class="alert success">{{ successMsg }}</div> }

  <div class="card table-wrap">
    @if (loading) {
      <p class="muted">{{ t('admin.currencies.loading') }}</p>
    } @else if (items.length === 0) {
      <p class="muted">{{ t('admin.currencies.empty') }}</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>{{ t('admin.currencies.code') }}</th>
            <th>{{ t('admin.currencies.name') }}</th>
            <th>{{ t('admin.currencies.symbol') }}</th>
            <th>{{ t('admin.currencies.scale') }}</th>
            <th>{{ t('admin.currencies.active') }}</th>
            <th>{{ t('admin.currencies.action') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (item of items; track item.id) {
            <tr>
              <td>{{ item.code }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.symbol }}</td>
              <td>{{ item.scale }}</td>
              <td><app-status-badge [value]="item.active ? 'ACTIVE' : 'DISABLED'"/></td>
              <td>
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
    .alert { padding: .75rem 1rem; border-radius: .75rem; margin-bottom: 1rem; }
    .alert.danger { background: #fee2e2; color: #991b1b; }
    .alert.success { background: #dcfce7; color: #166534; }
    .muted { color: #64748b; }
  `]
})
export class CurrenciesComponent implements OnInit {
  items: CurrencyResponse[] = [];
  loading = false;
  actionId: number | null = null;
  errorMsg = '';
  successMsg = '';

  constructor(private data: DataService, private i18n: I18nService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.data.currencies().subscribe({
      next: items => {
        this.items = items;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMsg = `${this.t('admin.currencies.load_error')} (${err?.status ?? '?'})`;
      }
    });
  }

  toggle(item: CurrencyResponse): void {
    this.actionId = item.id;
    this.data.updateCurrencyActivation(item.id, !item.active).subscribe({
      next: () => {
        this.actionId = null;
        this.successMsg = this.t('admin.currencies.status_success');
        this.load();
      },
      error: err => {
        this.actionId = null;
        this.errorMsg = err?.error?.message || this.t('admin.currencies.status_error');
      }
    });
  }

  t(key: string): string { return this.i18n.get(key); }
}
