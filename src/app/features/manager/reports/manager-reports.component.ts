import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { CommissionsReportResponse, TransfersReportResponse } from '../../../core/models/report.models';

@Component({
  selector: 'app-manager-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<section class="page">
  <h1 class="page-title">{{ t('manager.reports.title') }}</h1>
  <p class="page-subtitle">{{ t('manager.reports.description') }}</p>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }

  <div class="card">
    <button class="btn primary" type="button" (click)="load()" [disabled]="loading">
      {{ loading ? t('manager.reports.loading') : t('manager.reports.refresh') }}
    </button>
  </div>

  @if (loading) {
    <div class="card muted">{{ t('manager.reports.loading') }}</div>
  }

  @if (!loading && generatedAt) {
    <p class="generated-at">{{ t('manager.reports.generated_at') }}: <strong>{{ formatDate(generatedAt) }}</strong></p>
  }

  @if (transfersReport) {
    <div class="card">
      <h3>{{ t('manager.reports.transfers_summary') }}</h3>
      <p>{{ t('manager.reports.total_transfers') }}: <strong>{{ transfersReport.totalTransfers }}</strong></p>
      <p>{{ t('manager.reports.total_volume') }}: <strong>{{ transfersReport.totalVolume }}</strong></p>
      <p>{{ t('manager.reports.total_fees') }}: <strong>{{ transfersReport.totalFees }}</strong></p>
    </div>
  }

  @if (commissionsReport) {
    <div class="card">
      <h3>{{ t('manager.reports.commissions_summary') }}</h3>
      <p>{{ t('manager.reports.agency_commissions') }}: <strong>{{ commissionsReport.totalAgencyCommissions }}</strong></p>
      <p>{{ t('manager.reports.central_commissions') }}: <strong>{{ commissionsReport.totalCentralCommissions }}</strong></p>
    </div>
  }
</section>
`,
  styles: [`
    .alert.danger { background: #fee2e2; color: #991b1b; padding: .75rem 1rem; border-radius: .75rem; margin-bottom: 1rem; }
    .generated-at { margin: 0 0 1rem; color: #475569; font-size: .95rem; }
    .muted { color: #64748b; }
  `]
})
export class ManagerReportsComponent {
  loading = false;
  errorMsg = '';
  generatedAt = '';
  transfersReport: TransfersReportResponse | null = null;
  commissionsReport: CommissionsReportResponse | null = null;

  constructor(
    private data: DataService,
    public i18n: I18nService
  ) {}

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.transfersReport = null;
    this.commissionsReport = null;
    this.generatedAt = '';

    forkJoin({
      transfers: this.data.transfersReport({ format: 'JSON' }),
      commissions: this.data.commissionsReport({ format: 'JSON' })
    }).subscribe({
      next: ({ transfers, commissions }) => {
        this.transfersReport = transfers;
        this.commissionsReport = commissions;
        this.generatedAt = transfers.generatedAt || commissions.generatedAt || new Date().toISOString();
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        const status = err?.status ?? '?';
        const backendMsg = err?.error?.message;
        this.errorMsg = backendMsg
          ? `${this.t('manager.reports.load_error')} — ${backendMsg} (${status})`
          : `${this.t('manager.reports.load_error')} (${status}). ${this.t('manager.reports.redeploy_hint')}`;
      }
    });
  }

  formatDate(value: string): string {
    return new Date(value).toLocaleString();
  }

  t(key: string): string {
    return this.i18n.get(key);
  }
}
