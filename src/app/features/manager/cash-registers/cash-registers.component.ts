import { Component, OnInit } from '@angular/core';
import { DataService } from '../../../core/services/data.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
@Component({selector:'app-cash-registers',standalone:true,imports:[StatusBadgeComponent],template:`<section class="page"><h1 class="page-title">Caisses agence</h1><div class="card"><p>Solde courant: <b>{{cash?.currentBalance}} {{cash?.currencyCode}}</b></p><app-status-badge [value]="cash?.status || 'OPEN'"/></div></section>`})
export class CashRegistersComponent implements OnInit{cash:any;constructor(private data:DataService){}ngOnInit(){this.data.currentCash().subscribe(x=>this.cash=x)}}
