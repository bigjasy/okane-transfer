import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { TransferResponse } from '../../../core/models/transfer.models';
import { TRANSFER_STATUS, TransferStatus } from '../../../core/models/enums';

@Component({
  selector: 'app-transfers',
  standalone: true,
  imports: [FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('agent.transfers.title') }}</h1>
      <p class="page-subtitle">{{ t('agent.transfers.subtitle') }}</p>
    </div>
    <button class="btn" type="button" (click)="load()" [disabled]="loading">{{ t('agent.transfers.refresh') }}</button>
  </div>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (successMsg) { <div class="alert success">{{ successMsg }}</div> }

  <div class="card form-grid">
    <input [placeholder]="t('agent.transfers.search_reference')" [(ngModel)]="q" name="q">
    <select [(ngModel)]="status" name="status">
      <option value="">{{ t('agent.transfers.all') }}</option>
      @for (s of statuses; track s) { <option [value]="s">{{ s }}</option> }
    </select>
  </div>

  <div class="card table-wrap">
    <table>
      <tr>
        <th>{{ t('agent.transfers.reference') }}</th>
        <th>{{ t('agent.transfers.sender') }}</th>
        <th>{{ t('agent.transfers.beneficiary') }}</th>
        <th>{{ t('agent.transfers.sent') }}</th>
        <th>{{ t('agent.transfers.received') }}</th>
        <th>{{ t('agent.transfers.status') }}</th>
        <th>{{ t('agent.transfers.date') }}</th>
        <th>{{ t('agent.transfers.actions') }}</th>
      </tr>
      @for (transfer of filtered(); track transfer.reference) {
        <tr>
          <td>{{ transfer.reference }}</td>
          <td>{{ transfer.senderName }}</td>
          <td>{{ transfer.beneficiaryName }}</td>
          <td>{{ transfer.sentAmount }} {{ transfer.sourceCurrency }}</td>
          <td>{{ transfer.receivedAmount }} {{ transfer.targetCurrency }}</td>
          <td><app-status-badge [value]="transfer.status"/></td>
          <td>{{ transfer.createdAt }}</td>
          <td>
            @if (canCancel(transfer.status)) {
              <button class="btn danger" type="button" (click)="cancel(transfer)" [disabled]="actionRef === transfer.reference">
                {{ actionRef === transfer.reference ? t('agent.transfers.cancelling') : t('agent.transfers.cancel') }}
              </button>
            }
          </td>
        </tr>
      }
    </table>
  </div>
</section>
`,
  styles: [`
    .alert { margin-bottom: 1rem; padding: .75rem 1rem; border-radius: .75rem; }
    .alert.danger { background: #fee2e2; color: #991b1b; }
    .alert.success { background: #dcfce7; color: #166534; }
    .btn.danger { background: #dc2626; color: #fff; }
  `]
})
export class TransfersComponent implements OnInit {
  items: TransferResponse[] = [];
  q = '';
  status: '' | TransferStatus = '';
  statuses = TRANSFER_STATUS;
  loading = false;
  actionRef: string | null = null;
  errorMsg = '';
  successMsg = '';

  constructor(private data: DataService, public i18n: I18nService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.data.transfers().subscribe({
      next: x => {
        this.items = x;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMsg = this.t('agent.transfers.load_error');
      }
    });
  }

  filtered(): TransferResponse[] {
    return this.items.filter(t =>
      (!this.q || t.reference.toLowerCase().includes(this.q.toLowerCase())) &&
      (!this.status || t.status === this.status)
    );
  }

  canCancel(status: TransferStatus): boolean {
    return status === 'AVAILABLE' || status === 'PENDING_PAYMENT' || status === 'DRAFT';
  }

  cancel(transfer: TransferResponse): void {
    this.actionRef = transfer.reference;
    this.errorMsg = '';
    this.successMsg = '';
    this.data.cancelTransfer(transfer.reference).subscribe({
      next: updated => {
        this.items = this.items.map(item => item.reference === updated.reference ? updated : item);
        this.actionRef = null;
        this.successMsg = this.t('agent.transfers.cancel_success');
      },
      error: err => {
        this.actionRef = null;
        this.errorMsg = err?.error?.message || this.t('agent.transfers.cancel_error');
      }
    });
  }

  t(key: string): string {
    return this.i18n.get(key);
  }
}
