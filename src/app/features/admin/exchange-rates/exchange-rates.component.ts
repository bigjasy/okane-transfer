import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { ExchangeRateHistoryResponse } from '../../../core/models/referential.models';
import { ExchangeRateSyncResponse } from '../../../core/models/compliance.models';

@Component({
  selector: 'app-exchange-rates',
  standalone: true,
  imports: [FormsModule],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('admin.exchange_rates.title') }}</h1>
    </div>
    <button class="btn primary" type="button" (click)="syncExternal()" [disabled]="syncLoading">
      {{ syncLoading ? t('admin.exchange_rates.syncing') : t('admin.exchange_rates.sync_external') }}
    </button>
  </div>

  @if (syncMessage) {
    <div class="ok-box">{{ syncMessage }}</div>
  }

  <div class="grid cols-2">
    <div class="card">
      <h3>{{ t('admin.exchange_rates.conversion') }}</h3>
      <form class="grid" (ngSubmit)="convert()">
        <input name="a" type="number" [(ngModel)]="conv.amount">
        <input name="s" [(ngModel)]="conv.sourceCurrency">
        <input name="t" [(ngModel)]="conv.targetCurrency">
        <button class="btn primary">{{ t('admin.exchange_rates.convert') }}</button>
      </form>
      @if (result) {
        <div class="ok-box">{{ result.sourceAmount }} {{ result.sourceCurrency }} = {{ result.convertedAmount }} {{ result.targetCurrency }}</div>
      }
    </div>

    <div class="card table-wrap">
      <h3>{{ t('admin.exchange_rates.active_rates') }}</h3>
      <table>
        <thead>
          <tr>
            <th>{{ t('admin.exchange_rates.source') }}</th>
            <th>{{ t('admin.exchange_rates.target') }}</th>
            <th>{{ t('admin.exchange_rates.rate') }}</th>
            <th>{{ t('admin.exchange_rates.validity') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (r of rates; track r.id) {
            <tr>
              <td>{{ r.sourceCurrency }}</td>
              <td>{{ r.targetCurrency }}</td>
              <td>{{ r.rate }}</td>
              <td>{{ r.validFrom }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  </div>

  <div class="card table-wrap">
    <h3>{{ t('admin.exchange_rates.history') }}</h3>
    @if (history.length === 0) {
      <p class="muted">{{ t('admin.exchange_rates.history_empty') }}</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>{{ t('admin.exchange_rates.source') }}</th>
            <th>{{ t('admin.exchange_rates.target') }}</th>
            <th>{{ t('admin.exchange_rates.old_rate') }}</th>
            <th>{{ t('admin.exchange_rates.rate') }}</th>
            <th>{{ t('admin.exchange_rates.changed_at') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (item of history; track item.id) {
            <tr>
              <td>{{ item.sourceCurrencyCode }}</td>
              <td>{{ item.targetCurrencyCode }}</td>
              <td>{{ item.oldRate ?? '—' }}</td>
              <td>{{ item.newRate }}</td>
              <td>{{ item.changedAt }}</td>
            </tr>
          }
        </tbody>
      </table>
    }
  </div>
</section>`,
  styles: [`
    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1rem;
      flex-wrap: wrap;
    }
    .muted { color: var(--muted); }
  `]
})
export class ExchangeRatesComponent implements OnInit {
  rates: any[] = [];
  history: ExchangeRateHistoryResponse[] = [];
  conv = { sourceCurrency: 'MAD', targetCurrency: 'EUR', amount: 1000 };
  result: any;
  syncLoading = false;
  syncMessage = '';

  constructor(private data: DataService, private i18n: I18nService) {}

  ngOnInit(): void {
    this.loadRates();
    this.loadHistory();
  }

  convert(): void {
    this.data.convert(this.conv).subscribe(x => this.result = x);
  }

  syncExternal(): void {
    this.syncLoading = true;
    this.syncMessage = '';
    this.data.syncExternalRates().subscribe({
      next: (response: ExchangeRateSyncResponse) => {
        this.syncMessage = `${response.provider}: ${response.updatedCount} rate(s) updated`;
        this.syncLoading = false;
        this.loadRates();
        this.loadHistory();
      },
      error: () => {
        this.syncMessage = this.t('admin.exchange_rates.sync_error');
        this.syncLoading = false;
      }
    });
  }

  t(key: string): string {
    return this.i18n.get(key);
  }

  private loadRates(): void {
    this.data.rates().subscribe(x => this.rates = x);
  }

  private loadHistory(): void {
    this.data.exchangeRateHistory('MAD', 'EUR').subscribe(items => this.history = items);
  }
}
