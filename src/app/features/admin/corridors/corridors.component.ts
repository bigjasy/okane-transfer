import { Component, OnInit } from '@angular/core';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
@Component({ selector:'app-corridorscomponent', standalone:true, imports:[StatusBadgeComponent], template:`
<section class="page"><div class="page-header"><div><h1 class="page-title">Corridors</h1><p class="page-subtitle">Données alignées avec les DTOs backend.</p></div></div>
<div class="card table-wrap"><table><thead><tr><th>Source</th><th>Destination</th><th>Actif</th><th>Limite jour</th><th>Limite mois</th></tr></thead><tbody>@for(item of items; track $index){<tr><td>{{item.sourceCountry?.name}}</td><td>{{item.destinationCountry?.name}}</td><td><app-status-badge [value]="item.active ? 'ACTIVE' : 'DISABLED'"/></td><td>{{item.dailyLimit}}</td><td>{{item.monthlyLimit}}</td></tr>}</tbody></table></div></section>`})
export class CorridorsComponent implements OnInit { items:any[]=[]; constructor(private data:DataService){} ngOnInit():void{ this.data.corridors().subscribe(x=>this.items=x as any[]); } }
