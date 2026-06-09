import { Component, OnInit } from '@angular/core';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
@Component({ selector:'app-currenciescomponent', standalone:true, imports:[StatusBadgeComponent], template:`
<section class="page"><div class="page-header"><div><h1 class="page-title">Devises</h1><p class="page-subtitle">Données alignées avec les DTOs backend.</p></div></div>
<div class="card table-wrap"><table><thead><tr><th>Code</th><th>Nom</th><th>Symbole</th><th>Scale</th><th>Actif</th></tr></thead><tbody>@for(item of items; track $index){<tr><td>{{item.code}}</td><td>{{item.name}}</td><td>{{item.symbol}}</td><td>{{item.scale}}</td><td><app-status-badge [value]="item.active ? 'ACTIVE' : 'DISABLED'"/></td></tr>}</tbody></table></div></section>`})
export class CurrenciesComponent implements OnInit { items:any[]=[]; constructor(private data:DataService){} ngOnInit():void{ this.data.currencies().subscribe(x=>this.items=x as any[]); } }
