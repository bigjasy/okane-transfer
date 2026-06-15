import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { TransfersReportResponse, CommissionsReportResponse } from '../../../core/models/report.models';
import { CommissionResponse } from '../../../core/models/finance.models';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<section class="page">
  <h1 class="page-title">{{ t('admin.reports.title') }}</h1>
  <p class="page-subtitle">{{ t('admin.reports.subtitle') }}</p>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (infoMsg) { <div class="alert info">{{ infoMsg }}</div> }

  <div class="card">
    <form class="form-grid" (ngSubmit)="loadReports()">
      <label>
        <span>{{ t('admin.reports.format') }}</span>
        <select name="format" [(ngModel)]="format">
          <option value="JSON">JSON</option>
          <option value="PDF">PDF</option>
          <option value="CSV">CSV</option>
        </select>
      </label>
      <label>
        <span>{{ t('admin.reports.agency_filter') }}</span>
        <input name="agencyId" type="number" min="1" [(ngModel)]="agencyId" [placeholder]="t('admin.reports.agency_filter_placeholder')">
      </label>
      <button class="btn primary" type="submit" [disabled]="loading">
        {{ loading ? t('admin.reports.loading') : t('admin.reports.generate') }}
      </button>
    </form>
  </div>

  @if (loading) {
    <div class="card muted">{{ t('admin.reports.loading') }}</div>
  }

  @if (!loading && generatedAt) {
    <p class="generated-at">{{ t('admin.reports.generated_at') }}: <strong>{{ formatDate(generatedAt) }}</strong></p>
  }

  @if (!loading && hasLoaded && !transfersReport && !commissionsReport) {
    <div class="card muted">{{ t('admin.reports.empty') }}</div>
  }

  @if (transfersReport) {
    <div class="card">
      <h3>{{ t('admin.reports.transfers_summary') }}</h3>
      <div class="metrics">
        <div><span>{{ t('admin.reports.total_transfers') }}</span><strong>{{ transfersReport.totalTransfers }}</strong></div>
        <div><span>{{ t('admin.reports.total_volume') }}</span><strong>{{ transfersReport.totalVolume }}</strong></div>
        <div><span>{{ t('admin.reports.total_fees') }}</span><strong>{{ transfersReport.totalFees }}</strong></div>
      </div>
      @if (statusEntries.length === 0) {
        <p class="muted">{{ t('admin.reports.no_status_breakdown') }}</p>
      } @else {
        <table>
          <tr><th>{{ t('admin.reports.status') }}</th><th>{{ t('admin.reports.count') }}</th></tr>
          @for (entry of statusEntries; track entry[0]) {
            <tr><td>{{ entry[0] }}</td><td>{{ entry[1] }}</td></tr>
          }
        </table>
      }
    </div>
  }

  @if (commissionsReport) {
    <div class="card">
      <h3>{{ t('admin.reports.commissions_summary') }}</h3>
      <div class="metrics">
        <div><span>{{ t('admin.reports.agency_commissions') }}</span><strong>{{ commissionsReport.totalAgencyCommissions }}</strong></div>
        <div><span>{{ t('admin.reports.central_commissions') }}</span><strong>{{ commissionsReport.totalCentralCommissions }}</strong></div>
      </div>
    </div>
  }

  @if (commissionRows.length) {
    <div class="card table-wrap">
      <h3>{{ t('admin.reports.commissions_detail') }}</h3>
      <table>
        <tr>
          <th>{{ t('admin.reports.transfer_ref') }}</th>
          <th>{{ t('admin.reports.agency') }}</th>
          <th>{{ t('admin.reports.agency_commissions') }}</th>
          <th>{{ t('admin.reports.central_commissions') }}</th>
          <th>{{ t('admin.reports.currency') }}</th>
        </tr>
        @for (row of commissionRows; track row.id) {
          <tr>
            <td>{{ row.transferReference }}</td>
            <td>{{ row.agencyName || '—' }}</td>
            <td>{{ row.agencyPart }}</td>
            <td>{{ row.centralPart }}</td>
            <td>{{ row.currency }}</td>
          </tr>
        }
      </table>
    </div>
  }
</section>
`,
  styles: [`
    .metrics { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; margin-bottom: 1rem; }
    .metrics div { display: grid; gap: .25rem; }
    .metrics span { color: #64748b; font-size: .9rem; }
    .alert { padding: .75rem 1rem; border-radius: .75rem; margin-bottom: 1rem; }
    .alert.danger { background: #fee2e2; color: #991b1b; }
    .alert.info { background: #e0f2fe; color: #0c4a6e; }
    .muted { color: #64748b; }
    .generated-at { margin: 0 0 1rem; color: #475569; font-size: .95rem; }
    table { width: 100%; border-collapse: collapse; }
    th, td { text-align: left; padding: .5rem; border-bottom: 1px solid #e2e8f0; }
  `]
})
export class ReportsComponent {
  format = 'JSON';
  agencyId: number | null = null;
  loading = false;
  hasLoaded = false;
  errorMsg = '';
  infoMsg = '';
  generatedAt = '';
  transfersReport: TransfersReportResponse | null = null;
  commissionsReport: CommissionsReportResponse | null = null;
  commissionRows: CommissionResponse[] = [];
  statusEntries: [string, number][] = [];

  constructor(private data: DataService, public i18n: I18nService) {}

  loadReports(): void {
    this.loading = true;
    this.hasLoaded = true;
    this.errorMsg = '';
    this.infoMsg = '';
    this.transfersReport = null;
    this.commissionsReport = null;
    this.commissionRows = [];
    this.statusEntries = [];
    this.generatedAt = '';

    if (this.format !== 'JSON') {
      this.exportReports();
    }

    const params = {
      format: 'JSON',
      ...(this.agencyId && this.agencyId > 0 ? { agencyId: this.agencyId } : {})
    };

    forkJoin({
      transfers: this.data.transfersReport(params),
      commissions: this.data.commissionsReport(params),
      commissionList: this.data.commissions(0, 50, this.agencyId && this.agencyId > 0 ? this.agencyId : undefined)
    }).subscribe({
      next: ({ transfers, commissions, commissionList }) => {
        this.transfersReport = transfers;
        this.commissionsReport = commissions;
        this.commissionRows = commissionList.content;
        this.generatedAt = transfers.generatedAt || commissions.generatedAt || new Date().toISOString();
        this.statusEntries = Object.entries(transfers.transfersByStatus ?? {});
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        const status = err?.status ?? '?';
        const backendMsg = err?.error?.message;
        this.errorMsg = backendMsg
          ? `${this.t('admin.reports.load_error')} — ${backendMsg} (${status})`
          : `${this.t('admin.reports.load_error')} (${status}). ${this.t('admin.reports.redeploy_hint')}`;
      }
    });
  }

  formatDate(value: string): string {
    return new Date(value).toLocaleString();
  }

  private exportReports(): void {
    const exportParams: Record<string, string | number> = { format: this.format };
    if (this.agencyId && this.agencyId > 0) exportParams['agencyId'] = this.agencyId;

    const transfers$ = this.data.transfersReportBlob(exportParams);
    if (this.format === 'PDF') {
      forkJoin({
        transfers: transfers$,
        commissions: this.data.commissionsReportBlob(exportParams)
      }).subscribe({
        next: ({ transfers, commissions }) => {
          this.downloadBlob(transfers, 'transfers-report.pdf');
          this.downloadBlob(commissions, 'commissions-report.pdf');
          this.infoMsg = this.t('admin.reports.export_success', { format: this.format });
        },
        error: () => { this.errorMsg = this.t('admin.reports.export_error'); }
      });
      return;
    }

    if (this.format === 'CSV') {
      transfers$.subscribe({
        next: blob => {
          this.downloadBlob(blob, 'transfers-report.csv');
          this.infoMsg = this.t('admin.reports.export_success', { format: this.format });
        },
        error: () => { this.errorMsg = this.t('admin.reports.export_error'); }
      });
    }
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  t(key: string, vars?: Record<string, string>): string {
    return this.i18n.translate(key, vars);
  }
}

