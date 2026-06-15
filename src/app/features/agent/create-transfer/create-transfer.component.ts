import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { IDENTITY_TYPES, IdentityType, TRANSFER_CHANNELS } from '../../../core/models/enums';
import { AgencyResponse, CorridorResponse, FeeSimulationResponse } from '../../../core/models/agency.models';
import { CountryResponse } from '../../../core/models/referential.models';
import { TransferCreateRequest, TransferResponse } from '../../../core/models/transfer.models';

interface SenderDraft {
  firstName: string;
  lastName: string;
  identityType: IdentityType;
  identityNumber: string;
  phoneNumber: string;
  countryId: number;
}

type TransferResponseWithWithdrawalCode = TransferResponse & {
  withdrawalCode?: string;
  withdrawal_code?: string;
  codeRetrait?: string;
};

@Component({
  selector: 'app-create-transfer',
  standalone: true,
  imports: [FormsModule],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('agent.create_transfer.title') }}</h1>
      <p class="page-subtitle">{{ t('agent.create_transfer.subtitle') }}</p>
    </div>
  </div>

  @if (errorMsg) {
    <div class="error-box">{{ errorMsg }}</div>
  }

  <form class="transfer-form" (ngSubmit)="create()">
    <div class="grid cols-2">
      <div class="card">
        <h3>{{ t('agent.create_transfer.sender_section') }}</h3>
        <div class="form-grid">
          <div class="field">
            <label for="senderFirstName">{{ t('agent.create_transfer.sender_first_name') }}</label>
            <input id="senderFirstName" name="senderFirstName" [(ngModel)]="sender.firstName" autocomplete="given-name">
          </div>
          <div class="field">
            <label for="senderLastName">{{ t('agent.create_transfer.sender_last_name') }}</label>
            <input id="senderLastName" name="senderLastName" [(ngModel)]="sender.lastName" autocomplete="family-name">
          </div>
          <div class="field">
            <label for="senderIdentityType">{{ t('agent.create_transfer.identity_type') }}</label>
            <select id="senderIdentityType" name="senderIdentityType" [(ngModel)]="sender.identityType">
              @for (identityType of identityTypes; track identityType) {
                <option [value]="identityType">{{ identityType }}</option>
              }
            </select>
          </div>
          <div class="field">
            <label for="senderIdentityNumber">{{ t('agent.create_transfer.identity_number') }}</label>
            <input id="senderIdentityNumber" name="senderIdentityNumber" [(ngModel)]="sender.identityNumber">
          </div>
          <div class="field">
            <label for="senderPhone">{{ t('agent.create_transfer.sender_phone') }}</label>
            <input id="senderPhone" name="senderPhone" [(ngModel)]="sender.phoneNumber" type="tel" autocomplete="tel">
          </div>
          <div class="field">
            <label for="senderCountry">{{ t('agent.create_transfer.sender_country') }}</label>
            <select id="senderCountry" name="senderCountry" [(ngModel)]="sender.countryId">
              @if (countries.length === 0) {
                <option [ngValue]="sender.countryId">{{ countryLabel(sender.countryId) }}</option>
              }
              @for (country of countries; track country.id) {
                <option [ngValue]="country.id">{{ country.name }}</option>
              }
            </select>
          </div>
          <div class="field">
            <label for="senderClientId">{{ t('agent.create_transfer.sender_client_id') }}</label>
            <input id="senderClientId" name="senderClientId" type="number" [(ngModel)]="form.senderClientId">
          </div>
        </div>
      </div>

      <div class="card">
        <h3>{{ t('agent.create_transfer.beneficiary_section') }}</h3>
        <div class="form-grid">
          <div class="field">
            <label for="beneficiaryFirstName">{{ t('agent.create_transfer.beneficiary_first_name') }}</label>
            <input id="beneficiaryFirstName" name="beneficiaryFirstName" [(ngModel)]="form.beneficiary.firstName" autocomplete="given-name">
          </div>
          <div class="field">
            <label for="beneficiaryLastName">{{ t('agent.create_transfer.beneficiary_last_name') }}</label>
            <input id="beneficiaryLastName" name="beneficiaryLastName" [(ngModel)]="form.beneficiary.lastName" autocomplete="family-name">
          </div>
          <div class="field">
            <label for="beneficiaryPhone">{{ t('agent.create_transfer.phone') }}</label>
            <input id="beneficiaryPhone" name="beneficiaryPhone" [(ngModel)]="form.beneficiary.phoneNumber" type="tel" autocomplete="tel">
          </div>
          <div class="field">
            <label for="beneficiaryCountry">{{ t('agent.create_transfer.receiving_country') }}</label>
            <select id="beneficiaryCountry" name="beneficiaryCountry" [(ngModel)]="form.beneficiary.countryId">
              @if (countries.length === 0) {
                <option [ngValue]="form.beneficiary.countryId">{{ countryLabel(form.beneficiary.countryId) }}</option>
              }
              @for (country of countries; track country.id) {
                <option [ngValue]="country.id">{{ country.name }}</option>
              }
            </select>
          </div>
          <div class="field">
            <label for="beneficiaryIdentityType">{{ t('agent.create_transfer.identity_type') }}</label>
            <select id="beneficiaryIdentityType" name="beneficiaryIdentityType" [(ngModel)]="form.beneficiary.identityType">
              @for (identityType of identityTypes; track identityType) {
                <option [value]="identityType">{{ identityType }}</option>
              }
            </select>
          </div>
          <div class="field">
            <label for="beneficiaryIdentityNumber">{{ t('agent.create_transfer.identity_number') }}</label>
            <input id="beneficiaryIdentityNumber" name="beneficiaryIdentityNumber" [(ngModel)]="form.beneficiary.identityNumber">
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>{{ t('agent.create_transfer.transfer_section') }}</h3>
      <div class="form-grid">
        <div class="field">
          <label for="amount">{{ t('agent.create_transfer.amount') }}</label>
          <input id="amount" name="amount" type="number" min="0" step="0.01" [(ngModel)]="form.amount">
        </div>
        <div class="field">
          <label for="sourceCurrency">{{ t('agent.create_transfer.source_currency') }}</label>
          <select id="sourceCurrency" name="sourceCurrency" [(ngModel)]="form.sourceCurrency">
            @for (currency of currencyCodes; track currency) {
              <option [value]="currency">{{ currency }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="targetCurrency">{{ t('agent.create_transfer.target_currency') }}</label>
          <select id="targetCurrency" name="targetCurrency" [(ngModel)]="form.targetCurrency">
            @for (currency of currencyCodes; track currency) {
              <option [value]="currency">{{ currency }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="corridor">{{ t('agent.create_transfer.corridor') }}</label>
          <select id="corridor" name="corridor" [(ngModel)]="form.corridorId">
            @if (corridors.length === 0) {
              <option [ngValue]="form.corridorId">{{ corridorLabel(form.corridorId) }}</option>
            }
            @for (corridor of corridors; track corridor.id) {
              <option [ngValue]="corridor.id">{{ corridorLabel(corridor.id) }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="sourceAgency">{{ t('agent.create_transfer.source_agency') }}</label>
          <select id="sourceAgency" name="sourceAgency" [(ngModel)]="form.sourceAgencyId">
            @if (agencies.length === 0) {
              <option [ngValue]="form.sourceAgencyId">{{ agencyLabel(form.sourceAgencyId) }}</option>
            }
            @for (agency of agencies; track agency.id) {
              <option [ngValue]="agency.id">{{ agencyLabel(agency.id) }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="destinationAgency">{{ t('agent.create_transfer.destination_agency') }}</label>
          <select id="destinationAgency" name="destinationAgency" [(ngModel)]="form.destinationAgencyId">
            @if (agencies.length === 0) {
              <option [ngValue]="form.destinationAgencyId">{{ agencyLabel(form.destinationAgencyId) }}</option>
            }
            @for (agency of agencies; track agency.id) {
              <option [ngValue]="agency.id">{{ agencyLabel(agency.id) }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="channel">{{ t('agent.create_transfer.channel') }}</label>
          <select id="channel" name="channel" [(ngModel)]="form.channel">
            @for (channel of channels; track channel) {
              <option [value]="channel">{{ channel }}</option>
            }
          </select>
        </div>
      </div>
      <div class="form-actions">
        <button type="button" class="btn" (click)="simulate()" [disabled]="loadingSimulation">
          {{ loadingSimulation ? t('agent.create_transfer.processing') : t('agent.create_transfer.simulate_fees') }}
        </button>
        <button class="btn primary" [disabled]="creating">
          {{ creating ? t('agent.create_transfer.processing') : t('agent.create_transfer.create_transfer_btn') }}
        </button>
      </div>
    </div>
  </form>

  @if (simulation) {
    <div class="card fee-card">
      <h3>{{ t('agent.create_transfer.fee_section') }}</h3>
      <div class="metric-grid">
        <div class="metric">
          <span>{{ t('agent.create_transfer.sent_amount') }}</span>
          <strong>{{ formatMoney(simulation.amount, form.sourceCurrency) }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.create_transfer.fees') }}</span>
          <strong>{{ formatMoney(simulation.feeAmount, form.sourceCurrency) }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.create_transfer.exchange_rate') }}</span>
          <strong>{{ simulation.exchangeRate }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.create_transfer.total') }}</span>
          <strong>{{ formatMoney(simulation.totalToPay, form.sourceCurrency) }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.create_transfer.received_amount') }}</span>
          <strong>{{ formatMoney(simulation.receivedAmount, form.targetCurrency) }}</strong>
        </div>
      </div>
    </div>
  }

  @if (created) {
    <div class="card receipt-card" id="transfer-receipt">
      <div class="receipt-header">
        <h3>{{ t('agent.create_transfer.summary_section') }}</h3>
        <span class="reference">{{ created.reference }}</span>
      </div>

      <div class="summary-grid">
        <div class="summary-item wide">
          <span>{{ t('agent.create_transfer.sender') }}</span>
          <strong>{{ senderFullName }}</strong>
          <small>{{ sender.identityType }} {{ sender.identityNumber }} - {{ sender.phoneNumber }} - {{ countryLabel(sender.countryId) }}</small>
        </div>
        <div class="summary-item wide">
          <span>{{ t('agent.create_transfer.beneficiary') }}</span>
          <strong>{{ beneficiaryFullName }}</strong>
          <small>{{ form.beneficiary.phoneNumber }} - {{ countryLabel(form.beneficiary.countryId) }}</small>
        </div>
        <div class="summary-item">
          <span>{{ t('agent.create_transfer.sent_amount') }}</span>
          <strong>{{ formatMoney(created.sentAmount, created.sourceCurrency) }}</strong>
        </div>
        <div class="summary-item">
          <span>{{ t('agent.create_transfer.fees') }}</span>
          <strong>{{ formatMoney(created.feeAmount, created.sourceCurrency) }}</strong>
        </div>
        <div class="summary-item">
          <span>{{ t('agent.create_transfer.exchange_rate') }}</span>
          <strong>{{ created.exchangeRateApplied }}</strong>
        </div>
        <div class="summary-item">
          <span>{{ t('agent.create_transfer.received_amount') }}</span>
          <strong>{{ formatMoney(created.receivedAmount, created.targetCurrency) }}</strong>
        </div>
        <div class="summary-item">
          <span>{{ t('agent.create_transfer.status') }}</span>
          <strong>{{ created.status }}</strong>
        </div>
        <div class="summary-item">
          <span>{{ t('agent.create_transfer.withdrawal_code') }}</span>
          @if (withdrawalCode) {
            <strong>{{ withdrawalCode }}</strong>
          } @else {
            <strong class="placeholder">{{ t('agent.create_transfer.withdrawal_code_pending') }}</strong>
          }
        </div>
      </div>

      <div class="receipt-actions no-print">
        @if (created.status === 'PENDING_PAYMENT') {
          <button type="button" class="btn primary" (click)="confirmPayment()" [disabled]="confirmingPayment">
            {{ confirmingPayment ? t('agent.create_transfer.confirming_payment') : t('agent.create_transfer.confirm_payment') }}
          </button>
        }
        <button type="button" class="btn" (click)="printReceipt()">{{ t('agent.create_transfer.print_receipt') }}</button>
        <button type="button" class="btn" (click)="downloadReceiptPdf()" [disabled]="downloadingReceipt">
          {{ downloadingReceipt ? t('agent.create_transfer.downloading_receipt') : t('agent.create_transfer.download_receipt_pdf') }}
        </button>
        <button type="button" class="btn success" (click)="simulateReceiptSend()">{{ t('agent.create_transfer.simulate_receipt_send') }}</button>
      </div>

      @if (receiptMessage) {
        <div class="ok-box no-print">{{ receiptMessage }}</div>
      }
    </div>
  }
</section>

<style>
  h3 {
    margin: 0 0 1rem;
  }
  .transfer-form {
    display: grid;
    gap: 1.25rem;
  }
  .form-actions,
  .receipt-actions {
    display: flex;
    flex-wrap: wrap;
    gap: .75rem;
    margin-top: 1rem;
  }
  .metric-grid,
  .summary-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: .75rem;
  }
  .metric,
  .summary-item {
    display: grid;
    gap: .3rem;
    padding: .85rem;
    border: 1px solid var(--border);
    border-radius: .75rem;
    background: #f8fafc;
    min-width: 0;
  }
  .metric span,
  .summary-item span {
    color: var(--muted);
    font-size: .8rem;
    font-weight: 700;
  }
  .metric strong,
  .summary-item strong {
    overflow-wrap: anywhere;
  }
  .summary-item small {
    color: var(--muted);
    overflow-wrap: anywhere;
  }
  .summary-item.wide {
    grid-column: span 2;
  }
  .receipt-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 1rem;
  }
  .reference {
    display: inline-flex;
    padding: .35rem .65rem;
    border-radius: 999px;
    background: #dbeafe;
    color: #1e40af;
    font-weight: 800;
    white-space: nowrap;
  }
  .placeholder {
    color: var(--warning);
  }
  @media (max-width: 900px) {
    .metric-grid,
    .summary-grid {
      grid-template-columns: 1fr;
    }
    .summary-item.wide {
      grid-column: auto;
    }
    .receipt-header {
      align-items: flex-start;
      flex-direction: column;
    }
  }
  @media print {
    .transfer-form,
    .fee-card,
    .page-header,
    .error-box,
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
export class CreateTransferComponent implements OnInit {
  readonly identityTypes = IDENTITY_TYPES;
  readonly channels = TRANSFER_CHANNELS;

  countries: CountryResponse[] = [];
  agencies: AgencyResponse[] = [];
  corridors: CorridorResponse[] = [];
  currencyCodes = ['MAD', 'EUR', 'USD'];

  simulation?: FeeSimulationResponse;
  created?: TransferResponseWithWithdrawalCode;
  receiptMessage = '';
  errorMsg = '';
  loadingSimulation = false;
  creating = false;
  confirmingPayment = false;
  downloadingReceipt = false;

  sender: SenderDraft = {
    firstName: 'Client',
    lastName: 'Demo',
    identityType: 'CIN',
    identityNumber: 'CIN123456',
    phoneNumber: '+212600000000',
    countryId: 1
  };

  form: TransferCreateRequest = {
    senderClientId: 4,
    beneficiary: {
      firstName: 'Yassine',
      lastName: 'El Amrani',
      phoneNumber: '+33612345678',
      countryId: 2,
      identityType: 'PASSPORT',
      identityNumber: 'P123456'
    },
    sourceAgencyId: 1,
    destinationAgencyId: 2,
    corridorId: 1,
    sourceCurrency: 'MAD',
    targetCurrency: 'EUR',
    amount: 1200,
    channel: 'AGENCY'
  };

  constructor(private data: DataService, public i18n: I18nService) {}

  ngOnInit(): void {
    this.loadReferenceData();
  }

  simulate(): void {
    this.loadingSimulation = true;
    this.errorMsg = '';

    this.data.simulateFees({
      corridorId: this.form.corridorId,
      sourceCurrency: this.form.sourceCurrency,
      targetCurrency: this.form.targetCurrency,
      amount: this.form.amount
    }).subscribe({
      next: result => {
        this.simulation = result;
        this.loadingSimulation = false;
      },
      error: () => {
        this.errorMsg = this.t('agent.create_transfer.error_simulation');
        this.loadingSimulation = false;
      }
    });
  }

  create(): void {
    this.creating = true;
    this.errorMsg = '';
    this.receiptMessage = '';

    this.data.createTransfer(this.form).subscribe({
      next: transfer => {
        this.created = transfer;
        this.creating = false;
      },
      error: () => {
        this.errorMsg = this.t('agent.create_transfer.error_create');
        this.creating = false;
      }
    });
  }

  confirmPayment(): void {
    if (!this.created?.reference) return;
    this.confirmingPayment = true;
    this.errorMsg = '';

    this.data.confirmTransferPayment(this.created.reference).subscribe({
      next: (withdrawalCode) => {
        this.created = {
          ...this.created!,
          status: 'AVAILABLE',
          withdrawalCode
        };
        this.confirmingPayment = false;
      },
      error: (err) => {
        this.errorMsg = err?.error?.message || this.t('agent.create_transfer.error_confirm_payment');
        this.confirmingPayment = false;
      }
    });
  }

  printReceipt(): void {
    window.print();
  }

  downloadReceiptPdf(): void {
    if (!this.created?.reference) return;
    this.downloadingReceipt = true;
    this.data.sendReceiptBlob(this.created.reference, this.withdrawalCode || undefined).subscribe({
      next: blob => {
        this.saveBlob(blob, `send-receipt-${this.created!.reference}.pdf`);
        this.downloadingReceipt = false;
      },
      error: () => {
        this.errorMsg = this.t('agent.create_transfer.error_download_receipt');
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
    this.receiptMessage = this.t('agent.create_transfer.receipt_sent');
  }

  get senderFullName(): string {
    return this.fullName(this.sender.firstName, this.sender.lastName) || this.created?.senderName || '-';
  }

  get beneficiaryFullName(): string {
    return this.fullName(this.form.beneficiary.firstName, this.form.beneficiary.lastName) || this.created?.beneficiaryName || '-';
  }

  get withdrawalCode(): string | null {
    const code = this.created?.withdrawalCode ?? this.created?.withdrawal_code ?? this.created?.codeRetrait;
    const normalized = code?.trim();
    return normalized ? normalized : null;
  }

  countryLabel(id: number): string {
    return this.countries.find(country => country.id === id)?.name ?? `${this.t('agent.create_transfer.country')} #${id}`;
  }

  agencyLabel(id: number): string {
    const agency = this.agencies.find(item => item.id === id);
    return agency ? `${agency.name} (${agency.city})` : `${this.t('agent.create_transfer.agency')} #${id}`;
  }

  corridorLabel(id: number): string {
    const corridor = this.corridors.find(item => item.id === id);
    if (!corridor) return `${this.t('agent.create_transfer.corridor')} #${id}`;

    return `${corridor.sourceCountry.name} -> ${corridor.destinationCountry.name}`;
  }

  formatMoney(amount: number, currency: string): string {
    return `${amount.toFixed(2)} ${currency}`;
  }

  t(key: string): string {
    return this.i18n.get(key);
  }

  private loadReferenceData(): void {
    this.data.countries().subscribe({
      next: countries => {
        this.countries = countries;
      },
      error: () => {
        this.countries = [];
      }
    });

    this.data.agencies(0, 100).subscribe({
      next: result => {
        this.agencies = result.content;
      },
      error: () => {
        this.agencies = [];
      }
    });

    this.data.corridors().subscribe({
      next: corridors => {
        this.corridors = corridors;
      },
      error: () => {
        this.corridors = [];
      }
    });

    this.data.currencies().subscribe({
      next: currencies => {
        const codes = currencies.map(currency => currency.code);
        this.currencyCodes = codes.length > 0 ? codes : this.currencyCodes;
      },
      error: () => {
        this.currencyCodes = ['MAD', 'EUR', 'USD'];
      }
    });
  }

  private fullName(firstName: string, lastName: string): string {
    return [firstName, lastName].map(part => part.trim()).filter(Boolean).join(' ');
  }
}
