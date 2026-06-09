import { Component, OnInit } from '@angular/core';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
@Component({ selector:'app-countriescomponent', standalone:true, imports:[StatusBadgeComponent], template:`
<section class="page"><div class="page-header"><div><h1 class="page-title">Pays</h1><p class="page-subtitle">Données alignées avec les DTOs backend.</p></div></div>
<div class="card table-wrap"><table><thead><tr><th>ISO</th><th>Nom</th><th>Préfixe</th><th>Devise</th><th>Actif</th></tr></thead><tbody>@for(item of items; track $index){<tr><td>{{item.isoCode}}</td><td>{{item.name}}</td><td>{{item.phonePrefix}}</td><td>{{item.currency?.code}}</td><td><app-status-badge [value]="item.active ? 'ACTIVE' : 'DISABLED'"/></td></tr>}</tbody></table></div></section>`})
export class CountriesComponent implements OnInit { items:any[]=[]; constructor(private data:DataService){} ngOnInit():void{ this.data.countries().subscribe(x=>this.items=x as any[]); } }
