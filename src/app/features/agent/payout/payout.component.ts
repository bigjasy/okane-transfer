import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { IDENTITY_TYPES, IdentityType } from '../../../core/models/enums';
import { PayoutConfirmRequest, PayoutReceiptResponse, PayoutResponse, PayoutSearchResponse } from '../../../core/models/transfer.models';

@Component({
  selector: 'app-payout',
  standalone: true,
  imports: [FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('agent.payout.title') }}</h1>
      <p class="page-subtitle">{{ t('agent.payout.subtitle') }}</p>
    </div>
  </div>

  <div class="grid cols-3 payout-grid">
    <div class="card step-card">
      <h3>{{ t('agent.payout.step_1_search') }}</h3>
      <div class="field">
        <label for="withdrawalCode">{{ t('agent.payout.withdrawal_code') }}</label>
        <input id="withdrawalCode" name="withdrawalCode" [(ngModel)]="withdrawalCode" (ngModelChange)="resetSelectedTransfer()">
      </div>
      <div class="field">
        <label for="beneficiaryPhone">{{ t('agent.payout.beneficiary_phone') }}</label>
        <input id="beneficiaryPhone" name="beneficiaryPhone" [(ngModel)]="beneficiaryPhoneNumber" type="tel" (ngModelChange)="resetSelectedTransfer()">
      </div>
      <button class="btn primary" type="button" (click)="search()" [disabled]="!canSearch">
        {{ searchLoading ? t('agent.payout.searching') : t('agent.payout.search') }}
      </button>
      @if (searchError) {
        <div class="error-box compact">{{ searchError }}</div>
      }
    </div>

    <div class="card step-card">
      <h3>{{ t('agent.payout.step_2_identity') }}</h3>
      @if (selectedTransfer) {
        <div class="details">
          <div>
            <span>{{ t('agent.payout.transfer_reference') }}</span>
            <strong>{{ selectedReference }}</strong>
          </div>
          <div>
            <span>{{ t('agent.payout.sender') }}</span>
            <strong>{{ selectedTransfer.senderName || unavailableLabel }}</strong>
          </div>
          <div>
            <span>{{ t('agent.payout.beneficiary') }}</span>
            <strong>{{ selectedTransfer.beneficiaryName || unavailableLabel }}</strong>
          </div>
          <div>
            <span>{{ t('agent.payout.beneficiary_phone') }}</span>
            <strong>{{ selectedTransfer.beneficiaryPhoneNumber || beneficiaryPhoneNumber || unavailableLabel }}</strong>
          </div>
          <div>
            <span>{{ t('agent.payout.sent_amount') }}</span>
            <strong>{{ formatMoney(selectedTransfer.sentAmount, selectedTransfer.sourceCurrency) }}</strong>
          </div>
          <div>
            <span>{{ t('agent.payout.received_amount') }}</span>
            <strong>{{ formatMoney(selectedTransfer.receivedAmount, selectedTransfer.targetCurrency) }}</strong>
          </div>
          <div>
            <span>{{ t('agent.payout.status') }}</span>
            <app-status-badge [value]="selectedTransfer.status"/>
          </div>
          <div>
            <span>{{ t('agent.payout.expires_at') }}</span>
            <strong>{{ formatDate(selectedTransfer.expiresAt) }}</strong>
          </div>
        </div>

        @if (payoutBlockMessage) {
          <div class="error-box compact">{{ payoutBlockMessage }}</div>
        }
      } @else {
        <p class="muted">{{ t('agent.payout.search_first') }}</p>
      }

      <div class="field">
        <label for="identityType">{{ t('agent.payout.identity_type') }}</label>
        <select id="identityType" name="identityType" [(ngModel)]="identityType" (ngModelChange)="resetValidation()">
          @for (identity of identityTypes; track identity) {
            <option [value]="identity">{{ identity }}</option>
          }
        </select>
      </div>
      <div class="field">
        <label for="identityNumber">{{ t('agent.payout.identity_number') }}</label>
        <input id="identityNumber" name="identityNumber" [(ngModel)]="identityNumber" (ngModelChange)="resetValidation()">
      </div>
      <button class="btn" type="button" (click)="validate()" [disabled]="!canValidate">
        {{ validationLoading ? t('agent.payout.validating') : t('agent.payout.validate') }}
      </button>

      @if (validationMessage) {
        <div [class]="identityValidated ? 'ok-box compact' : 'error-box compact'">{{ validationMessage }}</div>
      }
      @if (validationError) {
        <div class="error-box compact">{{ validationError }}</div>
      }
    </div>

    <div class="card step-card">
      <h3>{{ t('agent.payout.step_3_confirm') }}</h3>
      @if (requiresOtp) {
        <div class="field">
          <label for="otpCode">{{ t('agent.payout.otp') }}</label>
          <input id="otpCode" name="otpCode" [(ngModel)]="otpCode" inputmode="numeric">
        </div>
        <p class="muted">{{ t('agent.payout.otp_required') }}</p>
      } @else {
        <p class="muted">{{ t('agent.payout.confirm_help') }}</p>
      }

      <button class="btn success" type="button" (click)="confirm()" [disabled]="!canConfirm">
        {{ confirming ? t('agent.payout.confirming') : t('agent.payout.pay') }}
      </button>

      @if (confirmError) {
        <div class="error-box compact">{{ confirmError }}</div>
      }
    </div>
  </div>

  @if (receipt) {
    <div class="card receipt-card" id="payout-receipt">
      <div class="receipt-header">
        <h3>{{ t('agent.payout.receipt_title') }}</h3>
        <span class="reference">{{ receipt.transferReference }}</span>
      </div>
      <div class="receipt-grid">
        <div>
          <span>{{ t('agent.payout.beneficiary') }}</span>
          <strong>{{ receipt.beneficiaryName || unavailableLabel }}</strong>
        </div>
        <div>
          <span>{{ t('agent.payout.paid_amount') }}</span>
          <strong>{{ formatMoney(receipt.paidAmount, receipt.currency) }}</strong>
        </div>
        <div>
          <span>{{ t('agent.payout.paid_at') }}</span>
          <strong>{{ formatDate(receipt.paidAt) }}</strong>
        </div>
        <div>
          <span>{{ t('agent.payout.status') }}</span>
          <app-status-badge [value]="receipt.status"/>
        </div>
        <div>
          <span>{{ t('agent.payout.agent') }}</span>
          <strong>{{ receipt.agentName || unavailableLabel }}</strong>
        </div>
        <div>
          <span>{{ t('agent.payout.agency') }}</span>
          <strong>{{ receipt.agencyName || unavailableLabel }}</strong>
        </div>
        <div>
          <span>{{ t('agent.payout.masked_identity_number') }}</span>
          <strong>{{ receipt.maskedIdentityNumber || maskIdentity(identityNumber) }}</strong>
        </div>
      </div>
      <div class="receipt-actions no-print">
        <button class="btn" type="button" (click)="printReceipt()">{{ t('agent.payout.print_payment_receipt') }}</button>
        <button class="btn" type="button" (click)="downloadReceiptPdf()" [disabled]="downloadingReceipt">
          {{ downloadingReceipt ? t('agent.payout.downloading_receipt') : t('agent.payout.download_receipt_pdf') }}
        </button>
        <button class="btn success" type="button" (click)="simulateReceiptSend()">{{ t('agent.payout.simulate_receipt_send') }}</button>
      </div>
      @if (receiptMessage) {
        <div class="ok-box compact no-print">{{ receiptMessage }}</div>
      }
    </div>
  }
</section>

<style>
  .payout-grid {
    align-items: start;
  }
  .step-card {
    display: grid;
    gap: .9rem;
  }
  .step-card h3,
  .receipt-card h3 {
    margin: 0;
  }
  .details,
  .receipt-grid {
    display: grid;
    gap: .65rem;
  }
  .details div,
  .receipt-grid div {
    display: grid;
    gap: .2rem;
    min-width: 0;
    padding: .65rem;
    border: 1px solid var(--border);
    border-radius: .75rem;
    background: #f8fafc;
  }
  .details span,
  .receipt-grid span {
    color: var(--muted);
    font-size: .78rem;
    font-weight: 700;
  }
  .details strong,
  .receipt-grid strong {
    overflow-wrap: anywhere;
  }
  .muted {
    margin: 0;
    color: var(--muted);
  }
  .compact {
    margin-top: .25rem;
  }
  .receipt-card {
    display: grid;
    gap: 1rem;
  }
  .receipt-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
  }
  .receipt-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .receipt-actions {
    display: flex;
    flex-wrap: wrap;
    gap: .75rem;
  }
  .reference {
    display: inline-flex;
    padding: .35rem .65rem;
    border-radius: 999px;
    background: #dbeafe;
    color: #1e40af;
    font-weight: 800;
  }
  @media (max-width: 900px) {
    .receipt-grid {
      grid-template-columns: 1fr;
    }
  }
  @media print {
    .page-header,
    .payout-grid,
    .no-print {
      display: none !important;
    }
    .receipt-card {
      box-shadow: none;
      border: 0;
    }
  }
</style>`
})
export class PayoutComponent {
  readonly identityTypes = IDENTITY_TYPES;

  withdrawalCode = '123456';
  beneficiaryPhoneNumber = '+33612345678';
  transferReference = '';
  identityType: IdentityType = 'PASSPORT';
  identityNumber = 'P123456';
  otpCode = '';

  selectedTransfer: PayoutSearchResponse | null = null;
  result: PayoutResponse | null = null;
  receipt: PayoutReceiptResponse | null = null;

  searchLoading = false;
  validationLoading = false;
  confirming = false;
  identityValidated = false;
  requiresOtp = false;

  searchError = '';
  validationError = '';
  validationMessage = '';
  confirmError = '';
  receiptMessage = '';
  downloadingReceipt = false;

  constructor(private data: DataService, public i18n: I18nService) {}

  get canSearch(): boolean {
    return !this.searchLoading && (!!this.withdrawalCode.trim() || !!this.beneficiaryPhoneNumber.trim());
  }

  get selectedReference(): string {
    return this.selectedTransfer?.transferReference || this.selectedTransfer?.reference || this.transferReference;
  }

  get unavailableLabel(): string {
    return this.t('agent.payout.unavailable_value');
  }

  get canValidate(): boolean {
    return !!this.selectedTransfer
      && this.isSelectedTransferPayable
      && !!this.withdrawalCode.trim()
      && !!this.identityType
      && !!this.identityNumber.trim()
      && !this.validationLoading;
  }

  get canConfirm(): boolean {
    return !!this.selectedTransfer
      && this.isSelectedTransferPayable
      && !!this.withdrawalCode.trim()
      && !!this.identityType
      && !!this.identityNumber.trim()
      && this.identityValidated
      && (!this.requiresOtp || !!this.otpCode.trim())
      && !this.confirming;
  }

  get isSelectedTransferPayable(): boolean {
    return this.isPayableStatus(this.selectedTransfer?.status);
  }

  get payoutBlockMessage(): string {
    const status = this.normalizeStatus(this.selectedTransfer?.status);
    if (!status || this.isPayableStatus(status)) return '';

    if (['PAID', 'PAYÉ', 'PAYE'].includes(status)) return this.t('agent.payout.already_paid');
    if (['CANCELLED', 'CANCELED', 'ANNULÉ', 'ANNULE'].includes(status)) return this.t('agent.payout.cancelled');
    if (['EXPIRED', 'EXPIRÉ', 'EXPIRE'].includes(status)) return this.t('agent.payout.expired');
    if (status === 'BLOCKED_AML') return this.t('agent.payout.blocked_aml');
    return this.t('agent.payout.not_available_for_payout');
  }

  search(): void {
    if (!this.canSearch) return;

    this.searchLoading = true;
    this.searchError = '';
    this.resetSelectionState();

    this.data.searchPayout({
      withdrawalCode: this.withdrawalCode.trim(),
      beneficiaryPhoneNumber: this.beneficiaryPhoneNumber.trim()
    }).subscribe({
      next: transfer => {
        this.selectedTransfer = transfer;
        this.transferReference = transfer.transferReference || transfer.reference;
        this.withdrawalCode = transfer.withdrawalCode || this.withdrawalCode;
        this.beneficiaryPhoneNumber = transfer.beneficiaryPhoneNumber || this.beneficiaryPhoneNumber;
        this.searchLoading = false;
      },
      error: error => {
        this.searchError = this.errorStatus(error) === 404
          ? this.t('agent.payout.error_not_found')
          : this.t('agent.payout.backend_unavailable');
        this.searchLoading = false;
      }
    });
  }

  validate(): void {
    if (!this.canValidate) return;

    this.validationLoading = true;
    this.validationError = '';
    this.validationMessage = '';
    this.identityValidated = false;
    this.requiresOtp = false;

    this.data.validatePayout({
      transferReference: this.selectedReference,
      withdrawalCode: this.withdrawalCode.trim(),
      identityType: this.identityType,
      identityNumber: this.identityNumber.trim()
    }).subscribe({
      next: response => {
        this.identityValidated = response.valid;
        this.requiresOtp = !!response.requiresOtp;
        this.validationMessage = response.valid
          ? (this.requiresOtp ? this.t('agent.payout.identity_validated_otp_required') : this.t('agent.payout.identity_validated'))
          : this.t('agent.payout.identity_invalid');
        this.validationLoading = false;
      },
      error: () => {
        this.validationError = this.t('agent.payout.backend_unavailable');
        this.validationLoading = false;
      }
    });
  }

  confirm(): void {
    if (!this.canConfirm) return;

    const request: PayoutConfirmRequest = {
      transferReference: this.selectedReference,
      withdrawalCode: this.withdrawalCode.trim(),
      identityType: this.identityType,
      identityNumber: this.identityNumber.trim(),
      otpCode: this.otpCode.trim()
    };

    this.confirming = true;
    this.confirmError = '';
    this.receiptMessage = '';

    this.data.confirmPayout(request).subscribe({
      next: response => {
        this.result = response;
        this.selectedTransfer = this.selectedTransfer ? { ...this.selectedTransfer, status: response.status } : this.selectedTransfer;
        this.confirming = false;
        this.loadReceipt(response);
      },
      error: () => {
        this.confirmError = this.t('agent.payout.backend_unavailable');
        this.confirming = false;
      }
    });
  }

  resetSelectedTransfer(): void {
    this.resetSelectionState();
    this.searchError = '';
  }

  resetValidation(): void {
    this.identityValidated = false;
    this.requiresOtp = false;
    this.validationError = '';
    this.validationMessage = '';
    this.otpCode = '';
    this.result = null;
    this.receipt = null;
    this.receiptMessage = '';
  }

  printReceipt(): void {
    window.print();
  }

  downloadReceiptPdf(): void {
    if (!this.receipt?.transferReference) return;
    this.downloadingReceipt = true;
    this.data.payoutReceiptBlob(this.receipt.transferReference).subscribe({
      next: blob => {
        this.saveBlob(blob, `payout-receipt-${this.receipt!.transferReference}.pdf`);
        this.downloadingReceipt = false;
      },
      error: () => {
        this.validationError = this.t('agent.payout.error_download_receipt');
        this.downloadingReceipt = false;
      }
    });
  }

  private saveBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  simulateReceiptSend(): void {
    this.receiptMessage = this.t('agent.payout.receipt_sent');
  }

  formatMoney(amount?: number, currency?: string): string {
    if (amount === undefined || amount === null) return this.unavailableLabel;
    return `${amount.toFixed(2)} ${currency || ''}`.trim();
  }

  formatDate(value?: string): string {
    if (!value) return this.unavailableLabel;
    return new Intl.DateTimeFormat(undefined, { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
  }

  maskIdentity(value: string): string {
    const trimmed = value.trim();
    if (!trimmed) return this.unavailableLabel;
    if (trimmed.length <= 4) return '****';
    return `${trimmed.slice(0, 2)}****${trimmed.slice(-2)}`;
  }

  t(key: string): string {
    return this.i18n.get(key);
  }

  private loadReceipt(response: PayoutResponse): void {
    this.receipt = this.buildLocalReceipt(response);

    this.data.payoutReceipt(response.transferReference).subscribe({
      next: receipt => {
        this.receipt = {
          ...this.receipt,
          ...receipt,
          beneficiaryName: receipt.beneficiaryName || this.receipt?.beneficiaryName || this.unavailableLabel,
          maskedIdentityNumber: receipt.maskedIdentityNumber || this.receipt?.maskedIdentityNumber
        };
      },
      error: () => {
        this.receipt = this.buildLocalReceipt(response);
      }
    });
  }

  private buildLocalReceipt(response: PayoutResponse): PayoutReceiptResponse {
    return {
      transferReference: response.transferReference,
      beneficiaryName: response.beneficiaryName || this.selectedTransfer?.beneficiaryName || this.unavailableLabel,
      paidAmount: response.paidAmount,
      currency: response.currency,
      paidAt: response.paidAt,
      status: response.status,
      agentName: response.agentName || this.selectedTransfer?.agentName,
      agencyName: response.agencyName || this.selectedTransfer?.agencyName,
      maskedIdentityNumber: response.maskedIdentityNumber || this.maskIdentity(this.identityNumber)
    };
  }

  private resetSelectionState(): void {
    this.selectedTransfer = null;
    this.transferReference = '';
    this.resetValidation();
    this.confirmError = '';
  }

  private isPayableStatus(status?: string): boolean {
    return ['AVAILABLE', 'EN_ATTENTE', 'PENDING'].includes(this.normalizeStatus(status));
  }

  private normalizeStatus(status?: string): string {
    return (status || '').trim().toUpperCase();
  }

  private errorStatus(error: unknown): number | null {
    if (typeof error !== 'object' || error === null || !('status' in error)) return null;
    const status = (error as { status?: unknown }).status;
    return typeof status === 'number' ? status : null;
  }
}
