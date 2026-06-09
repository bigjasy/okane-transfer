import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../core/services/data.service';
import { MobileMoneyRequest, MobileMoneyResponse } from '../../core/models/notification.models';
import { MobileMoneyOperator } from '../../core/models/enums';

@Component({
  selector: 'app-mobile-money',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <h1 class="page-title">Mobile Money</h1>
  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  <div class="card">
    <form class="form-grid" (ngSubmit)="submit()">
      <input name="ref" [(ngModel)]="form.transferReference" placeholder="Référence">
      <select name="op" [(ngModel)]="form.operator">
        <option value="ORANGE_MONEY">ORANGE_MONEY</option>
        <option value="WAVE">WAVE</option>
        <option value="MPESA">MPESA</option>
      </select>
      <input name="phone" [(ngModel)]="form.walletPhoneNumber" placeholder="Wallet phone">
      <button class="btn primary" [disabled]="loading">
        {{ loading ? 'Envoi...' : 'Envoyer opérateur' }}
      </button>
    </form>
  </div>
  @if (result) {
    <div class="card">
      <p>Transaction : <b>{{ result.operatorTransactionReference }}</b></p>
      <p>Opérateur : {{ result.operator }}</p>
      <app-status-badge [value]="result.status"/>
      <app-status-badge [value]="result.reconciliationStatus"/>
      <div style="margin-top:1rem">
        <button class="btn" (click)="callback()" [disabled]="loading">Simulate Callback</button>
        <button class="btn success" (click)="reconcile()" [disabled]="loading">Simulate Reconciliation</button>
      </div>
    </div>
  }
</section>`
})
export class MobileMoneyComponent {
  form: MobileMoneyRequest = { transferReference: 'OKN-2026-0001', operator: 'ORANGE_MONEY' as MobileMoneyOperator, walletPhoneNumber: '+212600000000' };
  result: MobileMoneyResponse | null = null;
  loading = false;
  errorMsg = '';

  constructor(private data: DataService) {}

  submit(): void {
    this.loading = true;
    this.errorMsg = '';
    this.data.mobileMoney(this.form).subscribe({
      next: x => { this.result = x; this.loading = false; },
      error: (err) => {
        this.loading = false;
        console.warn('[MobileMoney] submit error:', err?.status);
        this.errorMsg = err?.status === 404
          ? 'Endpoint Mobile Money non disponible côté backend.'
          : 'Erreur lors de l\'envoi mobile money (statut ' + (err?.status ?? '?') + ').';
      }
    });
  }

  callback(): void {
    if (!this.result) return;
    this.loading = true;
    this.errorMsg = '';
    this.data.mobileMoneyCallback(this.result.id).subscribe({
      next: x => { this.result = x; this.loading = false; },
      error: (err) => {
        this.loading = false;
        console.warn('[MobileMoney] callback error:', err?.status);
        this.errorMsg = err?.status === 404
          ? 'Endpoint Mobile Money non disponible côté backend.'
          : 'Erreur simulate callback (statut ' + (err?.status ?? '?') + ').';
      }
    });
  }

  reconcile(): void {
    if (!this.result) return;
    this.loading = true;
    this.errorMsg = '';
    this.data.mobileMoneyReconcile(this.result.id).subscribe({
      next: x => { this.result = x; this.loading = false; },
      error: (err) => {
        this.loading = false;
        console.warn('[MobileMoney] reconcile error:', err?.status);
        this.errorMsg = err?.status === 404
          ? 'Endpoint Mobile Money non disponible côté backend.'
          : 'Erreur reconciliation (statut ' + (err?.status ?? '?') + ').';
      }
    });
  }
}
