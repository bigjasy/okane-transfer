import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { CountryResponse } from '../../../core/models/referential.models';

@Component({
  selector: 'app-countries',
  standalone: true,
  imports: [CommonModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('admin.countries.title') }}</h1>
      <p class="page-subtitle">{{ t('admin.countries.subtitle') }}</p>
    </div>
    <button class="btn" type="button" (click)="load()" [disabled]="loading">{{ t('admin.countries.refresh') }}</button>
  </div>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (successMsg) { <div class="alert success">{{ successMsg }}</div> }

  <div class="card table-wrap">
    @if (loading) {
      <p class="muted">{{ t('admin.countries.loading') }}</p>
    } @else if (items.length === 0) {
      <p class="muted">{{ t('admin.countries.empty') }}</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>{{ t('admin.countries.iso') }}</th>
            <th>{{ t('admin.countries.name') }}</th>
            <th>{{ t('admin.countries.prefix') }}</th>
            <th>{{ t('admin.countries.currency') }}</th>
            <th>{{ t('admin.countries.active') }}</th>
            <th>{{ t('admin.countries.action') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (item of items; track item.id) {
            <tr>
              <td>{{ item.isoCode }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.phonePrefix }}</td>
              <td>{{ item.currency?.code }}</td>
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
export class CountriesComponent implements OnInit {
  items: CountryResponse[] = [];
  loading = false;
  actionId: number | null = null;
  errorMsg = '';
  successMsg = '';

  constructor(private data: DataService, private i18n: I18nService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.data.countries().subscribe({
      next: items => {
        this.items = items;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMsg = `${this.t('admin.countries.load_error')} (${err?.status ?? '?'})`;
      }
    });
  }

  toggle(item: CountryResponse): void {
    this.actionId = item.id;
    this.data.updateCountryActivation(item.id, !item.active).subscribe({
      next: () => {
        this.actionId = null;
        this.successMsg = this.t('admin.countries.status_success');
        this.load();
      },
      error: err => {
        this.actionId = null;
        this.errorMsg = err?.error?.message || this.t('admin.countries.status_error');
      }
    });
  }

  t(key: string): string { return this.i18n.get(key); }
}
