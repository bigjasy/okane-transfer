import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { ChartCardComponent } from '../../../shared/components/chart-card/chart-card.component';
import { DataService } from '../../../core/services/data.service';
@Component({ selector: 'app-clientdashboardcomponent', standalone: true, imports: [CommonModule, StatCardComponent, ChartCardComponent], template: `
<section class="page"><div class="page-header"><div><h1 class="page-title">Dashboard Client</h1><p class="page-subtitle">Vue synthétique conforme aux besoins OkaneTransfer.</p></div></div>
<div class="grid cols-4"><app-stat-card label="Volume" [value]="summary.totalVolume + ' DH'" hint="Volume total"/><app-stat-card label="Transferts" [value]="summary.transferCount" hint="Nombre d'opérations"/><app-stat-card label="Frais" [value]="summary.totalFees + ' DH'" hint="Frais collectés"/><app-stat-card label="Commissions" [value]="summary.totalCommissions + ' DH'" hint="Part agence + centrale"/></div>
<div class="grid cols-2"><app-chart-card title="Transferts par mois" [data]="lineData"/><app-chart-card title="Statuts" type="doughnut" [data]="pieData"/></div></section>` })
export class ClientDashboardComponent implements OnInit {
 summary:any = { totalVolume: 0, transferCount: 0, totalFees: 0, totalCommissions: 0 };
 lineData:any = { labels: ['Jan','Fév','Mar','Avr','Mai','Juin'], datasets: [{ label: 'Transferts', data: [0,0,0,0,0,0] }] };
 pieData:any = { labels: ['AVAILABLE','PAID','PENDING','CANCELLED'], datasets: [{ data: [0,0,0,0] }] };
 constructor(private data: DataService) {}
 ngOnInit(): void { this.data.dashboard('client').subscribe(s => { this.summary=s; this.lineData={labels:['Jan','Fév','Mar','Avr','Mai','Juin'],datasets:[{label:'Transferts',data:s.charts?.months ?? [10,20,30,40,50,60]}]}; this.pieData={labels:['AVAILABLE','PAID','PENDING','CANCELLED'],datasets:[{data:s.charts?.status ?? [1,1,1,1]}]}; }); }
}
