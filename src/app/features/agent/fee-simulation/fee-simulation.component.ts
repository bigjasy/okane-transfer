import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { FeeSimulationRequest, FeeSimulationResponse } from '../../../core/models/agency.models';
@Component({selector:'app-fee-simulation',standalone:true,imports:[FormsModule],template:`<section class="page"><h1 class="page-title">{{ t('agent.fee_simulation.title') }}</h1><div class="card"><form class="form-grid" (ngSubmit)="simulate()"><input name="corridor" type="number" [(ngModel)]="form.corridorId" [placeholder]="t('agent.fee_simulation.corridor_id')"><input name="source" [(ngModel)]="form.sourceCurrency" [placeholder]="t('agent.fee_simulation.source_currency')"><input name="target" [(ngModel)]="form.targetCurrency" [placeholder]="t('agent.fee_simulation.target_currency')"><input name="amount" type="number" [(ngModel)]="form.amount" [placeholder]="t('agent.fee_simulation.amount')"><button class="btn primary">{{ t('agent.fee_simulation.simulate') }}</button></form></div>@if(result){<div class="grid cols-4"><div class="card">{{ t('agent.fee_simulation.fees') }}: <b>{{result.feeAmount}}</b></div><div class="card">{{ t('agent.fee_simulation.total') }}: <b>{{result.totalToPay}}</b></div><div class="card">{{ t('agent.fee_simulation.rate') }}: <b>{{result.exchangeRate}}</b></div><div class="card">{{ t('agent.fee_simulation.received') }}: <b>{{result.receivedAmount}}</b></div></div>}</section>`})
export class FeeSimulationComponent{
  form:FeeSimulationRequest={corridorId:1,sourceCurrency:'MAD',targetCurrency:'EUR',amount:1000};
  result?:FeeSimulationResponse;
  constructor(private data:DataService, public i18n:I18nService){}
  simulate(){this.data.simulateFees(this.form).subscribe(x=>this.result=x)}
  t(key: string): string { return this.i18n.get(key); }
}
