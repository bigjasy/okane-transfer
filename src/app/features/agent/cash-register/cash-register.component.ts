import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { CASH_MOVEMENT_TYPES } from '../../../core/models/enums';
import { CashClosingRequest, CashMovementRequest, CashMovementResponse, CashRegisterOpenRequest, CashRegisterResponse } from '../../../core/models/finance.models';

@Component({
  selector: 'app-cash-register',
  standalone: true,
  imports: [FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('agent.cash_register.title') }}</h1>
      <p class="page-subtitle">{{ t('agent.cash_register.subtitle') }}</p>
    </div>
    <button type="button" class="btn" (click)="load()" [disabled]="currentLoading">
      {{ currentLoading ? t('agent.cash_register.loading') : t('agent.cash_register.refresh') }}
    </button>
  </div>

  @if (currentError) {
    <div class="error-box">{{ currentError }}</div>
  }
  @if (successMessage) {
    <div class="ok-box">{{ successMessage }}</div>
  }

  <div class="card">
    <div class="card-title-row">
      <h3>{{ t('agent.cash_register.current_cash') }}</h3>
      @if (cash) {
        <app-status-badge [value]="cash.status"/>
      }
    </div>

    @if (cash) {
      <div class="metrics-grid">
        <div class="metric">
          <span>{{ t('agent.cash_register.register_id') }}</span>
          <strong>#{{ cash.id }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.cash_register.agency') }}</span>
          <strong>{{ cash.agencyName || cash.agencyCode || unavailableLabel }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.cash_register.agent') }}</span>
          <strong>{{ cash.agentName || unavailableLabel }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.cash_register.currency_code') }}</span>
          <strong>{{ cash.currencyCode }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.cash_register.opening_balance') }}</span>
          <strong>{{ formatMoney(cash.openingBalance, cash.currencyCode) }}</strong>
        </div>
        <div class="metric balance">
          <span>{{ t('agent.cash_register.current_balance') }}</span>
          <strong>{{ formatMoney(cash.currentBalance, cash.currencyCode) }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.cash_register.opened_at') }}</span>
          <strong>{{ formatDate(cash.openedAt) }}</strong>
        </div>
        <div class="metric">
          <span>{{ t('agent.cash_register.closed_at') }}</span>
          <strong>{{ formatDate(cash.closedAt) }}</strong>
        </div>
      </div>
    } @else if (currentLoading) {
      <p class="muted">{{ t('agent.cash_register.loading_current') }}</p>
    } @else {
      <p class="muted">{{ t('agent.cash_register.no_open_cash') }}</p>
    }
  </div>

  <div class="grid cols-3">
    @if (!hasOpenCash) {
      <div class="card form-card">
        <h3>{{ t('agent.cash_register.open') }}</h3>
        <div class="field">
          <label for="agentId">{{ t('agent.cash_register.agent_id') }}</label>
          <input id="agentId" name="agentId" type="number" [(ngModel)]="openForm.agentId">
        </div>
        <div class="field">
          <label for="agencyId">{{ t('agent.cash_register.agency_id') }}</label>
          <input id="agencyId" name="agencyId" type="number" [(ngModel)]="openForm.agencyId">
        </div>
        <div class="field">
          <label for="openCurrency">{{ t('agent.cash_register.currency_code') }}</label>
          <input id="openCurrency" name="openCurrency" [(ngModel)]="openForm.currencyCode">
        </div>
        <div class="field">
          <label for="openingBalance">{{ t('agent.cash_register.opening_balance') }}</label>
          <input id="openingBalance" name="openingBalance" type="number" min="0" step="0.01" [(ngModel)]="openForm.openingBalance">
        </div>
        <button class="btn primary" type="button" (click)="openCash()" [disabled]="openLoading">
          {{ openLoading ? t('agent.cash_register.opening') : t('agent.cash_register.open_btn') }}
        </button>
        @if (openError) {
          <div class="error-box compact">{{ openError }}</div>
        }
      </div>
    }

    <div class="card form-card">
      <h3>{{ t('agent.cash_register.movement') }}</h3>
      @if (!hasOpenCash) {
        <p class="muted">{{ t('agent.cash_register.open_required_for_movement') }}</p>
      }
      <div class="field">
        <label for="movementType">{{ t('agent.cash_register.type') }}</label>
        <select id="movementType" name="movementType" [(ngModel)]="movementForm.type" [disabled]="!hasOpenCash">
          @for (type of movementTypes; track type) {
            <option [value]="type">{{ type }}</option>
          }
        </select>
      </div>
      <div class="field">
        <label for="movementAmount">{{ t('agent.cash_register.amount') }}</label>
        <input id="movementAmount" name="movementAmount" type="number" min="0" step="0.01" [(ngModel)]="movementForm.amount" [disabled]="!hasOpenCash">
      </div>
      <div class="field">
        <label for="movementCurrency">{{ t('agent.cash_register.currency_code') }}</label>
        <input id="movementCurrency" name="movementCurrency" [(ngModel)]="movementForm.currencyCode" [disabled]="!hasOpenCash">
      </div>
      <div class="field">
        <label for="movementReason">{{ t('agent.cash_register.reason') }}</label>
        <input id="movementReason" name="movementReason" [(ngModel)]="movementForm.reason" [disabled]="!hasOpenCash">
      </div>
      <button class="btn" type="button" (click)="addMovement()" [disabled]="!canAddMovement">
        {{ movementLoading ? t('agent.cash_register.adding_movement') : t('agent.cash_register.add_movement') }}
      </button>
      @if (movementActionError) {
        <div class="error-box compact">{{ movementActionError }}</div>
      }
    </div>

    @if (hasOpenCash) {
      <div class="card form-card">
        <h3>{{ t('agent.cash_register.close') }}</h3>
        <div class="field">
          <label for="countedAmount">{{ t('agent.cash_register.counted_amount') }}</label>
          <input id="countedAmount" name="countedAmount" type="number" min="0" step="0.01" [(ngModel)]="closingForm.countedAmount">
        </div>
        <div class="field">
          <label for="closeComment">{{ t('agent.cash_register.comment') }}</label>
          <input id="closeComment" name="closeComment" [(ngModel)]="closingForm.comment">
        </div>
        @if (discrepancyMessage) {
          <div class="warning-box">{{ discrepancyMessage }}</div>
        }
        <button class="btn danger" type="button" (click)="closeCash()" [disabled]="closeLoading">
          {{ closeLoading ? t('agent.cash_register.closing') : t('agent.cash_register.close_btn') }}
        </button>
        @if (closeError) {
          <div class="error-box compact">{{ closeError }}</div>
        }
      </div>
    }
  </div>

  @if (closingSummary) {
    <div class="card">
      <h3>{{ t('agent.cash_register.closing_summary') }}</h3>
      <div class="summary-grid">
        <div>
          <span>{{ t('agent.cash_register.system_balance') }}</span>
          <strong>{{ formatMoney(closingSummary.systemBalance, closingSummary.currencyCode) }}</strong>
        </div>
        <div>
          <span>{{ t('agent.cash_register.declared_balance') }}</span>
          <strong>{{ formatMoney(closingSummary.declaredBalance, closingSummary.currencyCode) }}</strong>
        </div>
        <div>
          <span>{{ t('agent.cash_register.discrepancy') }}</span>
          <strong>{{ formatMoney(closingSummary.discrepancy, closingSummary.currencyCode) }}</strong>
        </div>
      </div>
    </div>
  }

  <div class="card table-wrap">
    <div class="card-title-row">
      <h3>{{ t('agent.cash_register.movements') }}</h3>
      @if (movementsLoading) {
        <span class="muted">{{ t('agent.cash_register.loading_movements') }}</span>
      }
    </div>
    @if (movementsError) {
      <div class="error-box compact">{{ movementsError }}</div>
    }
    @if (movements.length > 0) {
      <table>
        <thead>
          <tr>
            <th>{{ t('agent.cash_register.type') }}</th>
            <th>{{ t('agent.cash_register.amount') }}</th>
            <th>{{ t('agent.cash_register.transfer_reference') }}</th>
            <th>{{ t('agent.cash_register.reason') }}</th>
            <th>{{ t('agent.cash_register.created_by') }}</th>
            <th>{{ t('agent.cash_register.date') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (movement of movements; track movement.id) {
            <tr>
              <td><app-status-badge [value]="movement.type"/></td>
              <td>{{ formatMoney(movement.amount, movement.currencyCode) }}</td>
              <td>{{ movement.transferReference || unavailableLabel }}</td>
              <td>{{ movement.reason }}</td>
              <td>{{ movement.createdByName || movement.createdBy || unavailableLabel }}</td>
              <td>{{ formatDate(movement.createdAt) }}</td>
            </tr>
          }
        </tbody>
      </table>
    } @else {
      <p class="muted">{{ t('agent.cash_register.no_movements') }}</p>
    }
  </div>
</section>

<style>
  .card-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1rem;
    flex-wrap: wrap;
  }
  .card-title-row h3,
  .form-card h3 {
    margin: 0;
  }
  .metrics-grid,
  .summary-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: .75rem;
  }
  .metric,
  .summary-grid div {
    display: grid;
    gap: .25rem;
    padding: .85rem;
    border: 1px solid var(--border);
    border-radius: .75rem;
    background: #f8fafc;
    min-width: 0;
  }
  .metric span,
  .summary-grid span {
    color: var(--muted);
    font-size: .78rem;
    font-weight: 700;
  }
  .metric strong,
  .summary-grid strong {
    overflow-wrap: anywhere;
  }
  .metric.balance strong {
    color: var(--success);
    font-size: 1.25rem;
  }
  .form-card {
    display: grid;
    gap: .9rem;
    align-self: start;
  }
  .muted {
    color: var(--muted);
    margin: 0;
  }
  .compact {
    margin-top: .25rem;
  }
  .warning-box {
    background: #fffbeb;
    color: #92400e;
    border: 1px solid #fde68a;
    padding: .8rem;
    border-radius: .9rem;
  }
  @media (max-width: 900px) {
    .metrics-grid,
    .summary-grid {
      grid-template-columns: 1fr;
    }
  }
</style>`
})
export class CashRegisterComponent implements OnInit {
  readonly movementTypes = CASH_MOVEMENT_TYPES;

  cash: CashRegisterResponse | null = null;
  movements: CashMovementResponse[] = [];

  openForm: CashRegisterOpenRequest = {
    agentId: 3,
    agencyId: 1,
    currencyCode: 'MAD',
    openingBalance: 10000
  };
  movementForm: CashMovementRequest = {
    type: 'ADJUSTMENT',
    amount: 100,
    currencyCode: 'MAD',
    reason: 'Correction'
  };
  closingForm: CashClosingRequest = {
    countedAmount: 0,
    comment: 'Clôture'
  };
  closingSummary: { systemBalance: number; declaredBalance: number; discrepancy: number; currencyCode: string } | null = null;

  currentLoading = false;
  movementsLoading = false;
  openLoading = false;
  movementLoading = false;
  closeLoading = false;

  currentError = '';
  movementsError = '';
  openError = '';
  movementActionError = '';
  closeError = '';
  successMessage = '';

  constructor(private data: DataService, public i18n: I18nService) {}

  ngOnInit(): void {
    this.load();
  }

  get hasOpenCash(): boolean {
    return this.cash?.status === 'OPEN';
  }

  get canAddMovement(): boolean {
    return this.hasOpenCash
      && !this.movementLoading
      && this.movementForm.amount > 0
      && !!this.movementForm.currencyCode.trim()
      && !!this.movementForm.reason.trim();
  }

  get unavailableLabel(): string {
    return this.t('agent.cash_register.unavailable_value');
  }

  get discrepancyMessage(): string {
    if (!this.cash) return '';

    const discrepancy = this.roundAmount(this.closingForm.countedAmount - this.cash.currentBalance);
    if (discrepancy === 0) return '';

    return `${this.t('agent.cash_register.discrepancy_detected')} ${this.formatMoney(discrepancy, this.cash.currencyCode)}`;
  }

  load(): void {
    this.currentLoading = true;
    this.currentError = '';
    this.successMessage = '';
    this.closingSummary = null;

    this.data.currentCash().subscribe({
      next: cash => {
        this.cash = cash;
        this.syncFormsWithCash(cash);
        this.currentLoading = false;
        this.loadMovements(cash.id);
      },
      error: () => {
        this.cash = null;
        this.movements = [];
        this.currentError = this.t('agent.cash_register.current_backend_unavailable');
        this.currentLoading = false;
      }
    });
  }

  openCash(): void {
    this.openLoading = true;
    this.openError = '';
    this.successMessage = '';
    this.closingSummary = null;

    this.data.openCash(this.openForm).subscribe({
      next: cash => {
        this.cash = cash;
        this.syncFormsWithCash(cash);
        this.openLoading = false;
        this.successMessage = this.t('agent.cash_register.open_success');
        this.loadMovements(cash.id);
      },
      error: () => {
        this.openError = this.t('agent.cash_register.open_backend_unavailable');
        this.openLoading = false;
      }
    });
  }

  addMovement(): void {
    if (!this.cash || !this.canAddMovement) return;

    this.movementLoading = true;
    this.movementActionError = '';
    this.successMessage = '';

    this.data.addMovement(this.cash.id, this.movementForm).subscribe({
      next: () => {
        this.movementLoading = false;
        this.successMessage = this.t('agent.cash_register.movement_success');
        this.load();
      },
      error: () => {
        this.movementActionError = this.t('agent.cash_register.movements_backend_unavailable');
        this.movementLoading = false;
      }
    });
  }

  closeCash(): void {
    if (!this.cash) return;

    const systemBalance = this.cash.currentBalance;
    const declaredBalance = this.closingForm.countedAmount;
    const currencyCode = this.cash.currencyCode;

    this.closeLoading = true;
    this.closeError = '';
    this.successMessage = '';

    this.data.closeCash(this.cash.id, this.closingForm).subscribe({
      next: cash => {
        this.cash = cash;
        this.syncFormsWithCash(cash);
        this.closeLoading = false;
        this.successMessage = this.t('agent.cash_register.close_success');
        this.closingSummary = {
          systemBalance,
          declaredBalance,
          discrepancy: this.roundAmount(declaredBalance - systemBalance),
          currencyCode
        };
        this.loadMovements(cash.id);
      },
      error: () => {
        this.closeError = this.t('agent.cash_register.close_backend_unavailable');
        this.closeLoading = false;
      }
    });
  }

  formatMoney(amount: number, currencyCode: string): string {
    return `${amount.toFixed(2)} ${currencyCode}`;
  }

  formatDate(value?: string): string {
    if (!value) return this.unavailableLabel;
    return new Intl.DateTimeFormat(undefined, { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
  }

  t(key: string): string {
    return this.i18n.get(key);
  }

  private loadMovements(cashRegisterId: number): void {
    this.movementsLoading = true;
    this.movementsError = '';

    this.data.movements(cashRegisterId).subscribe({
      next: movements => {
        this.movements = movements;
        this.movementsLoading = false;
      },
      error: () => {
        this.movements = [];
        this.movementsError = this.t('agent.cash_register.movements_backend_unavailable');
        this.movementsLoading = false;
      }
    });
  }

  private syncFormsWithCash(cash: CashRegisterResponse): void {
    this.movementForm = {
      ...this.movementForm,
      currencyCode: cash.currencyCode
    };
    this.closingForm = {
      ...this.closingForm,
      countedAmount: cash.currentBalance
    };
  }

  private roundAmount(value: number): number {
    return Math.round(value * 100) / 100;
  }
}
