import { Component, OnInit } from '@angular/core';
import { DataService } from '../../../core/services/data.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
@Component({selector:'app-agents',standalone:true,imports:[StatusBadgeComponent],template:`<section class="page"><h1 class="page-title">Agents de l’agence</h1><div class="card table-wrap"><table><tr><th>Nom</th><th>Email</th><th>Statut</th></tr>@for(a of agents; track a.id){<tr><td>{{a.fullName}}</td><td>{{a.email}}</td><td><app-status-badge [value]="a.status"/></td></tr>}</table></div></section>`})
export class AgentsComponent implements OnInit{agents:any[]=[];constructor(private data:DataService){}ngOnInit(){this.data.users().subscribe(u=>this.agents=u.filter(x=>x.role==='ROLE_AGENT'))}}
