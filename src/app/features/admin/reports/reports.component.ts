import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
@Component({selector:'app-reports',standalone:true,imports:[FormsModule],template:`<section class="page"><h1 class="page-title">Rapports</h1><div class="card"><form class="form-grid"><input type="date" name="from" [(ngModel)]="from"><input type="date" name="to" [(ngModel)]="to"><select name="format" [(ngModel)]="format"><option>JSON</option><option>PDF</option><option>CSV</option></select><button class="btn primary" type="button">Exporter {{format}}</button></form></div><div class="card"><p>Preview mock : rapports transferts, agences, commissions, AML.</p></div></section>`})
export class ReportsComponent{from='';to='';format='PDF'}
