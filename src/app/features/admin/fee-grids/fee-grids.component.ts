import { Component, OnInit } from '@angular/core';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
@Component({ selector:'app-feegridscomponent', standalone:true, imports:[StatusBadgeComponent], template:`
<section class="page"><div class="page-header"><div><h1 class="page-title">Grilles de frais</h1><p class="page-subtitle">Données alignées avec les DTOs backend.</p></div></div>
<div class="card table-wrap"><table><thead><tr><th>Min</th><th>Max</th><th>Fixe</th><th>Pourcentage</th><th>Active</th></tr></thead><tbody>@for(item of items; track $index){<tr><td>{{item.minAmount}}</td><td>{{item.maxAmount}}</td><td>{{item.fixedFee}}</td><td>{{item.percentageFee}}%</td><td><app-status-badge [value]="item.active ? 'ACTIVE' : 'DISABLED'"/></td></tr>}</tbody></table></div></section>`})
export class FeeGridsComponent implements OnInit { items:any[]=[]; constructor(private data:DataService){} ngOnInit():void{ this.data.feeGrids().subscribe(x=>this.items=x as any[]); } }
