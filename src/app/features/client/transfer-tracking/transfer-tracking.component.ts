import { Component, OnDestroy } from '@angular/core';
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
  @if (toastVisible) {
    <div
      class="tracking-toast"
      role="status"
      aria-live="polite"
      [class.error]="toastType === 'error'"
      [class.success]="toastType === 'success'"
      [class.info]="toastType === 'info'">
      {{ toastMessage }}
    </div>
  }

  <h1 class="page-title">Suivi transfert</h1>
  <div class="card form-grid">
    <input name="ref" [(ngModel)]="ref" placeholder="Référence transfert" (keyup.enter)="trackTransfer()">
    <button class="btn primary" (click)="trackTransfer()" [disabled]="loading">
      {{ loading ? 'Recherche en cours...' : 'Rechercher' }}
    </button>
  </div>
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
</section>`,
  styles: [`
    .tracking-toast {
      position: fixed;
      top: 20px;
      left: 20px;
      z-index: 9999;
      min-width: 280px;
      max-width: 420px;
      padding: 14px 18px;
      border-radius: 12px;
      background: #ffffff;
      color: #1f2937;
      box-shadow: 0 10px 30px rgba(15, 23, 42, 0.18);
      border-left: 5px solid #3b82f6;
      font-size: 14px;
      font-weight: 500;
      line-height: 1.45;
      animation: toastSlideIn 0.25s ease-out;
    }

    .tracking-toast.error {
      border-left-color: #ef4444;
    }

    .tracking-toast.success {
      border-left-color: #22c55e;
    }

    .tracking-toast.info {
      border-left-color: #3b82f6;
    }

    @keyframes toastSlideIn {
      from {
        opacity: 0;
        transform: translateX(-16px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }
  `]
})
export class TransferTrackingComponent implements OnDestroy {
  ref = '';
  result: TransferTrackingResponse | null = null;
  loading = false;
  toastMessage = '';
  toastType: 'error' | 'success' | 'info' = 'error';
  toastVisible = false;
  private toastTimer?: ReturnType<typeof setTimeout>;

  constructor(private data: DataService) {}

  ngOnDestroy(): void {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
  }

  trackTransfer(): void {
    const reference = this.ref.trim();
    this.result = null;
    this.hideToast();

    if (!reference) {
      this.showToast('Veuillez saisir une référence de transfert.', 'error');
      return;
    }

    this.loading = true;
    this.data.trackTransfer(reference).subscribe({
      next: x => { this.result = x; this.loading = false; },
      error: (err) => {
        this.loading = false;
        this.showToast(
          err?.status === 404
            ? 'Aucun transfert trouvé avec cette référence.'
            : 'Une erreur est survenue lors du suivi du transfert.',
          'error'
        );
      }
    });
  }

  private showToast(message: string, type: 'error' | 'success' | 'info' = 'error'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.toastVisible = true;

    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }

    this.toastTimer = setTimeout(() => {
      this.toastVisible = false;
    }, 4000);
  }

  private hideToast(): void {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
      this.toastTimer = undefined;
    }
    this.toastVisible = false;
  }
}
