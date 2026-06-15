import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { ChartCardComponent } from '../../../shared/components/chart-card/chart-card.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
@Component({ selector: 'app-managerdashboardcomponent', standalone: true, imports: [CommonModule, StatCardComponent, ChartCardComponent], template: `
<section class="page"><div class="page-header"><div><h1 class="page-title">{{ t('manager.dashboard.title') }}</h1><p class="page-subtitle">{{ t('manager.dashboard.subtitle') }}</p></div></div>
<div class="grid cols-4"><app-stat-card [label]="t('manager.dashboard.volume')" [value]="summary.totalVolume + ' DH'" [hint]="t('manager.dashboard.total_volume')"/><app-stat-card [label]="t('manager.dashboard.transfers')" [value]="summary.transferCount" [hint]="t('manager.dashboard.operations_count')"/><app-stat-card [label]="t('manager.dashboard.fees')" [value]="summary.totalFees + ' DH'" [hint]="t('manager.dashboard.total_fees')"/><app-stat-card [label]="t('manager.dashboard.commissions')" [value]="summary.totalCommissions + ' DH'" [hint]="t('manager.dashboard.agency_central_share')"/></div>
<div class="grid cols-2"><app-chart-card [title]="t('manager.dashboard.transfers_by_month')" [data]="lineData"/><app-chart-card [title]="t('manager.dashboard.statuses')" type="doughnut" [data]="pieData"/></div></section>` })
export class ManagerDashboardComponent implements OnInit {
 summary:any = { totalVolume: 0, transferCount: 0, totalFees: 0, totalCommissions: 0 };
 lineData:any = { labels: ['Jan','Fév','Mar','Avr','Mai','Juin'], datasets: [{ label: 'Transferts', data: [0,0,0,0,0,0] }] };
 pieData:any = { labels: ['AVAILABLE','PAID','PENDING','CANCELLED'], datasets: [{ data: [0,0,0,0] }] };
 constructor(private data: DataService, public i18n: I18nService) {}
 ngOnInit(): void { this.data.dashboard('manager').subscribe(s => { this.summary=s; this.lineData={labels:['Jan','Fév','Mar','Avr','Mai','Juin'],datasets:[{label:this.i18n.get('manager.dashboard.transfers'),data:s.charts?.months ?? [10,20,30,40,50,60]}]}; this.pieData={labels:['AVAILABLE','PAID','PENDING','CANCELLED'],datasets:[{data:s.charts?.status ?? [1,1,1,1]}]}; }); }
 t(key: string): string { return this.i18n.get(key); }
}
