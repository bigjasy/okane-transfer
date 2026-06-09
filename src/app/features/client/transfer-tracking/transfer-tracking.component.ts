import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { TransferTrackingResponse } from '../../../core/models/transfer.models';

@Component({
  selector: 'app-transfer-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <h1 class="page-title">Suivi transfert</h1>
  <div class="card form-grid">
    <input name="ref" [(ngModel)]="ref" placeholder="Référence transfert">
    <button class="btn primary" (click)="track()" [disabled]="loading">
      {{ loading ? 'Recherche...' : 'Rechercher' }}
    </button>
  </div>
  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (result) {
    <div class="card">
      <h3>{{ result.reference }}</h3>
      <p>{{ result.sourceCountry }} → {{ result.destinationCountry }}</p>
      <p>Montant reçu : {{ result.receivedAmount }}</p>
      <app-status-badge [value]="result.status"/>
      <p>Créé : {{ result.createdAt }}</p>
      <p>Payé : {{ result.paidAt || '-' }}</p>
    </div>
  }
</section>`
})
export class TransferTrackingComponent {
  ref = '';
  result: TransferTrackingResponse | null = null;
  loading = false;
  errorMsg = '';

  constructor(private data: DataService) {}

  track(): void {
    if (!this.ref.trim()) return;
    this.loading = true;
    this.result = null;
    this.errorMsg = '';
    this.data.trackTransfer(this.ref.trim()).subscribe({
      next: x => { this.result = x; this.loading = false; },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.status === 404
          ? 'Endpoint backend de suivi de transfert non disponible.'
          : 'Transfert introuvable ou erreur serveur.';
      }
    });
  }
}
