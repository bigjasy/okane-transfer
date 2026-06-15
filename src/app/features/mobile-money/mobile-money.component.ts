import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../core/services/data.service';
import { I18nService } from '../../core/services/i18n.service';
import { AuthService } from '../../core/services/auth.service';
import { MobileMoneyReconciliationResponse, MobileMoneyRequest, MobileMoneyResponse } from '../../core/models/notification.models';
import { MobileMoneyOperator, Role } from '../../core/models/enums';

type RecentTransfer = MobileMoneyResponse & { walletPhoneNumber: string };

@Component({
  selector: 'app-mobile-money',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <h1 class="page-title">{{ t('mobile_money.title') }}</h1>
    <p class="page-subtitle">{{ t('mobile_money.subtitle') }}</p>
  </div>

  <div class="info-message">
    <p>{{ t('mobile_money.page_clarification') }}</p>
  </div>

  @if (errorMsg) {
    <div class="alert danger">{{ errorMsg }}</div>
  }

  @if (!canSendTransfer && currentUserRole === 'ROLE_CLIENT') {
    <div class="alert info">
      <p>{{ t('mobile_money.client_restriction') }}</p>
    </div>
  }

  @if (canSendTransfer) {
    <div class="grid cols-2">
      <div class="card">
        <h3>{{ t('mobile_money.form_title') }}</h3>
        <form class="form-grid" (ngSubmit)="submit()">
          <div>
            <label>{{ t('mobile_money.transfer_reference') }}</label>
            <input name="ref" [(ngModel)]="form.transferReference" type="text" [placeholder]="t('mobile_money.transfer_reference_placeholder')">
          </div>
          <div>
            <label>{{ t('mobile_money.operator') }}</label>
            <select name="op" [(ngModel)]="form.operator">
              <option value="ORANGE_MONEY">{{ t('mobile_money.orange_money') }}</option>
              <option value="WAVE">{{ t('mobile_money.wave') }}</option>
              <option value="MPESA">{{ t('mobile_money.mpesa') }}</option>
            </select>
          </div>
          <div>
            <label>{{ t('mobile_money.wallet_phone') }}</label>
            <input name="phone" [(ngModel)]="form.walletPhoneNumber" type="tel" [placeholder]="t('mobile_money.wallet_phone_placeholder')">
          </div>
          <button class="btn primary" [disabled]="loading" type="submit">
            {{ loading ? t('mobile_money.sending') : t('mobile_money.send') }}
          </button>
        </form>
      </div>

      @if (result) {
        <div class="card">
          <h3>{{ t('mobile_money.result_title') }}</h3>
          <div class="result-content">
            <div class="result-row">
              <span class="label">{{ t('mobile_money.mobile_money_id') }}:</span>
              <span class="value">{{ result.id }}</span>
            </div>
            <div class="result-row">
              <span class="label">{{ t('mobile_money.transfer_reference') }}:</span>
              <span class="value">{{ result.transferReference }}</span>
            </div>
            <div class="result-row">
              <span class="label">{{ t('mobile_money.operator') }}:</span>
              <span class="value">{{ getOperatorLabel(result.operator) }}</span>
            </div>
            <div class="result-row">
              <span class="label">{{ t('mobile_money.wallet_phone') }}:</span>
              <span class="value">{{ result.walletPhoneNumber }}</span>
            </div>
            @if (result.operatorTransactionReference) {
              <div class="result-row">
                <span class="label">{{ t('mobile_money.operator_transaction_ref') }}:</span>
                <span class="value">{{ result.operatorTransactionReference }}</span>
              </div>
            }
            <div class="result-row">
              <span class="label">{{ t('common.status') }}:</span>
              <span class="value"><app-status-badge [value]="result.status"/></span>
            </div>
            <div class="result-row">
              <span class="label">{{ t('mobile_money.reconciliation_status') }}:</span>
              <span class="value"><app-status-badge [value]="result.reconciliationStatus"/></span>
            </div>
          </div>

          @if (reconciliationSummary) {
            <div class="info-message">
              <p>Réconciliation : {{ reconciliationSummary.reconciled }} rapprochée(s), {{ reconciliationSummary.mismatches }} écart(s)</p>
            </div>
          }

          @if (smsNotificationShown) {
            <div class="sms-notification">
              <p>📱 {{ t('mobile_money.sms_sent') }} <strong>{{ result.walletPhoneNumber }}</strong></p>
            </div>
          }

          <div class="action-buttons">
            @if (canSimulateCallback) {
              <button class="btn" (click)="callback()" [disabled]="loading">
                {{ loading ? t('mobile_money.processing') : t('mobile_money.simulate_callback') }}
              </button>
            }
            @if (canSimulateReconciliation) {
              <button class="btn success" (click)="reconcile()" [disabled]="loading">
                {{ loading ? t('mobile_money.processing') : t('mobile_money.simulate_reconciliation') }}
              </button>
            }
          </div>
        </div>
      }
    </div>
  }

  @if (canViewRecentTransfers) {
    <div class="card table-wrap">
      <h3>{{ t('mobile_money.recent_transfers') }}</h3>
      @if (recentTransfers.length > 0) {
        <table>
          <tr>
            <th>{{ t('mobile_money.transfer_reference') }}</th>
            <th>{{ t('mobile_money.operator') }}</th>
            <th>{{ t('mobile_money.wallet_phone') }}</th>
            <th>{{ t('common.status') }}</th>
            <th>{{ t('mobile_money.reconciliation_status') }}</th>
            <th>{{ t('common.date') }}</th>
            @if (hasRecentTransferActions) {
              <th>{{ t('mobile_money.actions') }}</th>
            }
          </tr>
          @for (transfer of recentTransfers; track transfer.id) {
            <tr>
              <td>{{ transfer.transferReference }}</td>
              <td>{{ getOperatorLabel(transfer.operator) }}</td>
              <td>{{ transfer.walletPhoneNumber }}</td>
              <td><app-status-badge [value]="transfer.status"/></td>
              <td><app-status-badge [value]="transfer.reconciliationStatus"/></td>
              <td>{{ transfer.createdAt | date:'short' }}</td>
              @if (hasRecentTransferActions) {
                <td>
                  <div class="table-actions">
                    @if (canSimulateCallback) {
                      <button class="btn table-action" type="button" (click)="callback(transfer)" [disabled]="loading">
                        {{ isTransferActionLoading(transfer) ? t('mobile_money.processing') : t('mobile_money.simulate_callback') }}
                      </button>
                    }
                    @if (canSimulateReconciliation) {
                      <button class="btn success table-action" type="button" (click)="reconcile(transfer)" [disabled]="loading">
                        {{ isTransferActionLoading(transfer) ? t('mobile_money.processing') : t('mobile_money.simulate_reconciliation') }}
                      </button>
                    }
                  </div>
                </td>
              }
            </tr>
          }
        </table>
      } @else {
        <p>{{ t('mobile_money.no_transfers') }}</p>
      }
    </div>
  }

</section>

<style>
  .page-header {
    margin-bottom: 1.5rem;
  }
  .page-title {
    font-size: 1.8rem;
    font-weight: 700;
    margin: 0 0 0.5rem 0;
  }
  .page-subtitle {
    color: #666;
    margin: 0;
  }
  .info-message {
    background: #e3f2fd;
    border-left: 4px solid #2196f3;
    padding: 1rem;
    margin-bottom: 1.5rem;
    border-radius: 4px;
  }
  .info-message p {
    margin: 0;
    color: #1976d2;
  }
  .form-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  .form-grid > div {
    display: flex;
    flex-direction: column;
  }
  .form-grid label {
    font-weight: 600;
    margin-bottom: 0.5rem;
    color: #333;
  }
  .form-grid input,
  .form-grid select {
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
  }
  .result-content {
    display: grid;
    gap: 1rem;
    margin: 1.5rem 0;
  }
  .result-row {
    display: grid;
    grid-template-columns: 150px 1fr;
    gap: 1rem;
    align-items: center;
  }
  .result-row .label {
    font-weight: 600;
    color: #333;
  }
  .result-row .value {
    word-break: break-word;
  }
  .sms-notification {
    background: #e8f5e9;
    border-left: 4px solid #4caf50;
    padding: 1rem;
    margin: 1.5rem 0;
    border-radius: 4px;
  }
  .sms-notification p {
    margin: 0;
    color: #2e7d32;
  }
  .action-buttons {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
    margin-top: 1.5rem;
  }
  .action-buttons.single {
    grid-template-columns: 1fr;
  }
  .btn {
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.3s;
  }
  .btn.primary {
    background: #2196f3;
    color: white;
  }
  .btn.primary:hover:not(:disabled) {
    background: #1976d2;
  }
  .btn.success {
    background: #4caf50;
    color: white;
  }
  .btn.success:hover:not(:disabled) {
    background: #388e3c;
  }
  .btn:not(.primary):not(.success) {
    background: #f5f5f5;
    color: #333;
    border: 1px solid #ddd;
  }
  .btn:not(.primary):not(.success):hover:not(:disabled) {
    background: #e0e0e0;
  }
  .btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  .alert {
    padding: 1rem;
    border-radius: 4px;
    margin-bottom: 1.5rem;
  }
  .alert.danger {
    background: #ffebee;
    color: #c62828;
    border-left: 4px solid #f44336;
  }
  .alert.info {
    background: #e3f2fd;
    color: #1565c0;
    border-left: 4px solid #2196f3;
  }
  .alert.info p {
    margin: 0;
  }
  .card {
    background: #fff;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 1.5rem;
    margin-bottom: 1.5rem;
  }
  .card h3 {
    margin: 0 0 1rem 0;
    font-size: 1.2rem;
    font-weight: 600;
  }
  .grid {
    display: grid;
    gap: 1.5rem;
    margin-bottom: 1.5rem;
  }
  .grid.cols-2 {
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  }
  .table-wrap table {
    width: 100%;
    border-collapse: collapse;
  }
  .table-wrap th,
  .table-wrap td {
    padding: 0.75rem;
    text-align: left;
    border-bottom: 1px solid #ddd;
  }
  .table-wrap th {
    font-weight: 600;
    background: #f5f5f5;
  }
  .table-wrap tr:hover {
    background: #fafafa;
  }
  .table-actions {
    display: flex;
    gap: 0.5rem;
    align-items: center;
  }
  .btn.table-action {
    padding: 0.5rem 0.75rem;
    white-space: nowrap;
  }
</style>`
})
export class MobileMoneyComponent implements OnInit {
  form: MobileMoneyRequest = {
    transferReference: '',
    operator: 'ORANGE_MONEY' as MobileMoneyOperator,
    walletPhoneNumber: '+212600000000'
  };
  result: (MobileMoneyResponse & { walletPhoneNumber?: string }) | null = null;
  reconciliationSummary: MobileMoneyReconciliationResponse | null = null;
  recentTransfers: RecentTransfer[] = [];
  loading = false;
  errorMsg = '';
  smsNotificationShown = false;
  currentUserRole: Role | null = null;
  activeActionTransferId: number | null = null;

  canSendTransfer = false;
  canViewRecentTransfers = false;
  canSimulateCallback = false;
  canSimulateReconciliation = false;

  get hasRecentTransferActions(): boolean {
    return this.canSimulateCallback || this.canSimulateReconciliation;
  }

  constructor(
    private data: DataService,
    public i18n: I18nService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.setupRolePermissions();
    if (this.canViewRecentTransfers) {
      this.loadRecentTransfers();
    }
    this.prefillAvailableTransferReference();
  }

  private prefillAvailableTransferReference(): void {
    if (!this.canSendTransfer || this.form.transferReference) return;
    this.data.transfers().subscribe({
      next: (transfers) => {
        const available = transfers.find(t => t.status === 'AVAILABLE');
        if (available?.reference) {
          this.form.transferReference = available.reference;
        }
      }
    });
  }

  private setupRolePermissions(): void {
    const user = this.auth.user();
    this.currentUserRole = user?.role || null;

    // ROLE_AGENT: Can send Mobile Money transfers
    this.canSendTransfer = this.currentUserRole === 'ROLE_AGENT';

    // ROLE_ADMIN / ROLE_MANAGER / ROLE_AGENT: Can view Mobile Money transfers
    this.canViewRecentTransfers = this.currentUserRole === 'ROLE_ADMIN'
      || this.currentUserRole === 'ROLE_MANAGER'
      || this.currentUserRole === 'ROLE_AGENT';

    // ROLE_ADMIN: Can simulate callback
    this.canSimulateCallback = this.currentUserRole === 'ROLE_ADMIN';

    // ROLE_MANAGER / ROLE_ADMIN: Can run reconciliation
    this.canSimulateReconciliation = this.currentUserRole === 'ROLE_MANAGER'
      || this.currentUserRole === 'ROLE_ADMIN';
  }

  submit(): void {
    if (!this.canSendTransfer) return;

    this.loading = true;
    this.errorMsg = '';
    this.smsNotificationShown = false;

    this.data.mobileMoney(this.form).subscribe({
      next: (x) => {
        this.result = { ...x, walletPhoneNumber: this.form.walletPhoneNumber };
        this.reconciliationSummary = null;
        this.loading = false;
        this.smsNotificationShown = true;
        this.addToRecentTransfers();
      },
      error: (err) => {
        this.loading = false;
        console.warn('[MobileMoney] submit error:', err?.status, err?.error);
        if (err?.status === 404) {
          this.errorMsg = this.t('mobile_money.transfer_not_found');
        } else if (err?.status >= 500) {
          this.errorMsg = this.t('mobile_money.server_error');
        } else {
          const msg = err?.error?.message;
          this.errorMsg = msg
            ? `${this.t('mobile_money.error_sending')} ${err?.status ?? '?'}): ${msg}`
            : `${this.t('mobile_money.error_sending')} ${err?.status ?? '?'}`;
        }
      }
    });
  }

  callback(transfer?: RecentTransfer): void {
    if (!this.canSimulateCallback) return;

    const transferId = transfer?.id ?? this.result?.id;
    if (!transferId) return;

    this.loading = true;
    this.errorMsg = '';
    this.activeActionTransferId = transfer?.id ?? null;

    this.data.mobileMoneyCallback(transferId).subscribe({
      next: (x) => {
        this.applyMobileMoneyUpdate(x, transfer);
        this.loading = false;
        this.activeActionTransferId = null;
      },
      error: (err) => {
        this.loading = false;
        this.activeActionTransferId = null;
        console.warn('[MobileMoney] callback error:', err?.status);
        if (err?.status === 404) {
          this.errorMsg = this.t('mobile_money.backend_unavailable');
        } else {
          this.errorMsg = `${this.t('mobile_money.error_callback')} ${err?.status ?? '?'}`;
        }
      }
    });
  }

  reconcile(transfer?: RecentTransfer): void {
    if (!this.canSimulateReconciliation) return;

    const operator = transfer?.operator ?? this.result?.operator;
    if (!operator) return;

    this.loading = true;
    this.errorMsg = '';
    this.activeActionTransferId = transfer?.id ?? null;

    this.data.mobileMoneyReconcile(operator).subscribe({
      next: (summary) => {
        this.reconciliationSummary = summary;
        this.applyMobileMoneyReconciliationUpdate(transfer);
        this.loadRecentTransfers();
        this.loading = false;
        this.activeActionTransferId = null;
      },
      error: (err) => {
        this.loading = false;
        this.activeActionTransferId = null;
        console.warn('[MobileMoney] reconcile error:', err?.status);
        if (err?.status === 404) {
          this.errorMsg = this.t('mobile_money.backend_unavailable');
        } else {
          this.errorMsg = `${this.t('mobile_money.error_reconciliation')} ${err?.status ?? '?'}`;
        }
      }
    });
  }

  getOperatorLabel(operator: MobileMoneyOperator): string {
    const labels: Record<MobileMoneyOperator, string> = {
      ORANGE_MONEY: this.t('mobile_money.orange_money'),
      WAVE: this.t('mobile_money.wave'),
      MPESA: this.t('mobile_money.mpesa')
    };
    return labels[operator] || operator;
  }

  isTransferActionLoading(transfer: RecentTransfer): boolean {
    return this.loading && this.activeActionTransferId === transfer.id;
  }

  private addToRecentTransfers(): void {
    if (!this.result) return;
    const transfer: RecentTransfer = {
      ...this.result,
      walletPhoneNumber: this.form.walletPhoneNumber,
      createdAt: this.result.createdAt ?? new Date().toISOString()
    };
    this.recentTransfers = [transfer, ...this.recentTransfers.filter(item => item.id !== transfer.id)].slice(0, 20);
  }

  private applyMobileMoneyUpdate(response: MobileMoneyResponse, transfer?: RecentTransfer): void {
    const walletPhoneNumber = transfer?.walletPhoneNumber ?? this.result?.walletPhoneNumber ?? this.form.walletPhoneNumber;
    const updatedResult = { ...response, walletPhoneNumber };

    if (!this.result || this.result.id === response.id) {
      this.result = updatedResult;
    }

    this.recentTransfers = this.recentTransfers.map(item => {
      if (item.id !== response.id) return item;

      return {
        ...item,
        transferReference: response.transferReference,
        operator: response.operator,
        status: response.status,
        reconciliationStatus: response.reconciliationStatus
      };
    });
  }

  private applyMobileMoneyReconciliationUpdate(transfer?: RecentTransfer): void {
    const resultId = this.result?.id;
    const targetId = transfer?.id ?? resultId;

    if (this.result && (!targetId || this.result.id === targetId)) {
      this.result = { ...this.result, reconciliationStatus: 'RECONCILED' };
    }

    if (!targetId) return;

    this.recentTransfers = this.recentTransfers.map(item =>
      item.id === targetId ? { ...item, reconciliationStatus: 'RECONCILED' } : item
    );
  }

  private loadRecentTransfers(): void {
    if (!this.canViewRecentTransfers) return;

    this.data.mobileMoneyList(0, 20).subscribe({
      next: (items) => {
        this.recentTransfers = items.map(item => ({
          ...item,
          walletPhoneNumber: item.walletPhoneNumber ?? ''
        }));
      },
      error: (err) => {
        console.warn('[MobileMoney] list error:', err?.status, err?.error);
        if (err?.status >= 500) {
          this.errorMsg = this.t('mobile_money.server_error');
        }
      }
    });
  }

  t(key: string): string {
    return this.i18n.get(key);
  }
}
