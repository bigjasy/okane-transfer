import { Component, OnInit } from '@angular/core';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
@Component({ selector:'app-agenciescomponent', standalone:true, imports:[StatusBadgeComponent], template:`
<section class="page"><div class="page-header"><div><h1 class="page-title">Agences</h1><p class="page-subtitle">Données alignées avec les DTOs backend.</p></div></div>
<div class="card table-wrap"><table><thead><tr><th>Code</th><th>Nom</th><th>Ville</th><th>Pays</th><th>Statut</th><th>Limite</th></tr></thead><tbody>@for(item of items; track $index){<tr><td>{{item.code}}</td><td>{{item.name}}</td><td>{{item.city}}</td><td>{{item.country}}</td><td><app-status-badge [value]="item.status"/></td><td>{{item.dailyLimit}}</td></tr>}</tbody></table></div></section>`})
export class AgenciesComponent implements OnInit { items:any[]=[]; constructor(private data:DataService){} ngOnInit():void{ this.data.agencies().subscribe(x=>this.items=x as any[]); } }
