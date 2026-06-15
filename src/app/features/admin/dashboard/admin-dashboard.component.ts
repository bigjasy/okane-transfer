import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { ChartCardComponent } from '../../../shared/components/chart-card/chart-card.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, StatCardComponent, ChartCardComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('admin.dashboard.title') }}</h1>
      <p class="page-subtitle">{{ t('admin.dashboard.subtitle') }}</p>
    </div>
  </div>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (loading) { <p class="muted">{{ t('common.loading') }}</p> }

  <div class="grid cols-4">
    <app-stat-card [label]="t('admin.dashboard.volume')" [value]="formatAmount(summary.totalVolume)" [hint]="t('admin.dashboard.total_volume')"/>
    <app-stat-card [label]="t('admin.dashboard.transfers')" [value]="summary.transferCount" [hint]="t('admin.dashboard.operations_count')"/>
    <app-stat-card [label]="t('admin.dashboard.fees')" [value]="formatAmount(summary.totalFees)" [hint]="t('admin.dashboard.total_fees')"/>
    <app-stat-card [label]="t('admin.dashboard.commissions')" [value]="formatAmount(summary.totalCommissions)" [hint]="t('admin.dashboard.agency_central_share')"/>
  </div>

  <div class="grid cols-2">
    <app-chart-card [title]="t('admin.dashboard.transfers_by_month')" [data]="lineData"/>
    <app-chart-card [title]="t('admin.dashboard.statuses')" type="doughnut" [data]="pieData"/>
  </div>
</section>`,
  styles: [`
    .alert.danger { background: #fee2e2; color: #991b1b; padding: .75rem 1rem; border-radius: .75rem; margin-bottom: 1rem; }
    .muted { color: #64748b; }
  `]
})
export class AdminDashboardComponent implements OnInit {
  summary: any = { totalVolume: 0, transferCount: 0, totalFees: 0, totalCommissions: 0 };
  lineData: any = { labels: [], datasets: [{ label: '', data: [] }] };
  pieData: any = { labels: [], datasets: [{ data: [] }] };
  loading = false;
  errorMsg = '';

  constructor(private data: DataService, public i18n: I18nService) {}

  ngOnInit(): void {
    this.loading = true;
    this.data.dashboard('admin').subscribe({
      next: s => {
        this.summary = s;
        const charts = s.charts ?? {};
        const monthLabels = charts.monthLabels ?? ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin'];
        const monthData = charts.volumeByMonth ?? charts.months ?? [0, 0, 0, 0, 0, 0];
        const statusLabels = charts.statusLabels ?? Object.keys(charts.statusDistribution ?? {});
        const statusData = charts.status ?? Object.values(charts.statusDistribution ?? {});
        this.lineData = {
          labels: monthLabels,
          datasets: [{ label: this.t('admin.dashboard.transfers'), data: monthData }]
        };
        this.pieData = {
          labels: statusLabels.length ? statusLabels : [this.t('common.pending')],
          datasets: [{ data: statusData.length ? statusData : [0] }]
        };
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMsg = `${this.t('admin.dashboard.load_error')} (${err?.status ?? '?'})`;
      }
    });
  }

  formatAmount(value: unknown): string {
    if (value === null || value === undefined || value === '') return '0 MAD';
    return `${value} MAD`;
  }

  t(key: string): string { return this.i18n.get(key); }
}
