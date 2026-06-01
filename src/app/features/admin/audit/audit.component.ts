import { Component, OnInit } from '@angular/core';
import { DataService } from '../../../core/services/data.service';
@Component({selector:'app-audit',standalone:true,template:`<section class="page"><h1 class="page-title">Audit logs</h1><div class="card table-wrap"><table><tr><th>Acteur</th><th>Action</th><th>Entité</th><th>IP</th><th>Date</th></tr>@for(l of logs; track $index){<tr><td>{{l.actorEmail}}</td><td>{{l.action}}</td><td>{{l.entityType}} #{{l.entityId}}</td><td>{{l.ipAddress}}</td><td>{{l.createdAt}}</td></tr>}</table></div></section>`})
export class AuditComponent implements OnInit{logs:any[]=[];constructor(private data:DataService){}ngOnInit(){this.data.auditLogs().subscribe(x=>this.logs=x)}}
