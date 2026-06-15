import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { AmlAlertResponse, ComplianceSummaryResponse, KycDocumentResponse, KycReviewRequest } from '../../../core/models/compliance.models';
import { AmlAlertStatus } from '../../../core/models/enums';

type KycReviewDecision = Extract<KycReviewRequest['status'], 'APPROVED' | 'REJECTED'>;

@Component({
  selector: 'app-compliance',
  standalone: true,
  imports: [FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <h1 class="page-title">{{ t('admin.compliance.title') }}</h1>

  @if (summary) {
    <div class="grid cols-5 summary-grid">
      <div class="card metric"><span>{{ t('admin.compliance.summary.pending_kyc') }}</span><strong>{{ summary.pendingKycDocuments }}</strong></div>
      <div class="card metric"><span>{{ t('admin.compliance.summary.open_alerts') }}</span><strong>{{ summary.openAmlAlerts }}</strong></div>
      <div class="card metric"><span>{{ t('admin.compliance.summary.critical_alerts') }}</span><strong>{{ summary.criticalAmlAlerts }}</strong></div>
      <div class="card metric"><span>{{ t('admin.compliance.summary.watchlist') }}</span><strong>{{ summary.activeWatchlistEntries }}</strong></div>
      <div class="card metric"><span>{{ t('admin.compliance.summary.blocked') }}</span><strong>{{ summary.blockedTransfers }}</strong></div>
    </div>
  }

  <div class="grid cols-2">
    <div class="card table-wrap">
      <div class="card-header">
        <h3>{{ t('admin.compliance.kyc.pending_title') }}</h3>
        <button class="btn" type="button" (click)="loadKyc()" [disabled]="kycLoading">
          {{ kycLoading ? t('admin.compliance.kyc.loading') : t('admin.compliance.kyc.refresh') }}
        </button>
      </div>

      @if (kycWarning) {
        <div class="warning-box">{{ kycWarning }}</div>
      }
      @if (kycError) {
        <div class="error-box">{{ kycError }}</div>
      }
      @if (reviewMessage) {
        <div class="ok-box">{{ reviewMessage }}</div>
      }
      @if (reviewError) {
        <div class="error-box">{{ reviewError }}</div>
      }

      @if (kycLoading) {
        <p class="muted">{{ t('admin.compliance.kyc.loading') }}</p>
      } @else if (!kycError && kycDocuments.length === 0) {
        <p class="muted">{{ t('admin.compliance.kyc.empty') }}</p>
      } @else if (kycDocuments.length > 0) {
        <table>
          <thead>
            <tr>
              <th>{{ t('admin.compliance.kyc.id') }}</th>
              <th>{{ t('admin.compliance.kyc.client') }}</th>
              <th>{{ t('admin.compliance.kyc.document_type') }}</th>
              <th>{{ t('admin.compliance.kyc.status') }}</th>
              <th>{{ t('admin.compliance.kyc.uploaded_at') }}</th>
              <th>{{ t('admin.compliance.kyc.rejection_reason') }}</th>
              <th>{{ t('admin.compliance.kyc.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            @for (document of kycDocuments; track document.id) {
              <tr>
                <td>#{{ document.id }}</td>
                <td>{{ ownerLabel(document) }}</td>
                <td>{{ document.documentType }}</td>
                <td><app-status-badge [value]="document.status"/></td>
                <td>{{ formatDate(document.uploadedAt) }}</td>
                <td>
                  <input
                    class="reason-input"
                    [name]="'reason-' + document.id"
                    [(ngModel)]="rejectionReasons[document.id]"
                    [placeholder]="t('admin.compliance.kyc.rejection_reason_placeholder')"
                    [disabled]="isReviewing(document.id)">
                  @if (document.rejectionReason) {
                    <small>{{ document.rejectionReason }}</small>
                  }
                </td>
                <td>
                  <div class="actions">
                    <button class="btn primary" type="button" (click)="reviewDocument(document, 'APPROVED')" [disabled]="isReviewing(document.id)">
                      {{ t('admin.compliance.kyc.approve') }}
                    </button>
                    <button class="btn danger" type="button" (click)="reviewDocument(document, 'REJECTED')" [disabled]="!canReject(document)">
                      {{ t('admin.compliance.kyc.reject') }}
                    </button>
                  </div>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }
    </div>

    <div class="card">
      <div class="card-header">
        <h3>{{ t('admin.compliance.aml_alerts') }}</h3>
        <button class="btn" type="button" (click)="loadAml()">{{ t('admin.compliance.kyc.refresh') }}</button>
      </div>
      @if (amlReviewMessage) { <div class="ok-box">{{ amlReviewMessage }}</div> }
      @if (amlError) { <div class="error-box">{{ amlError }}</div> }
      <table>
        <thead>
          <tr>
            <th>{{ t('admin.compliance.reference') }}</th>
            <th>{{ t('admin.compliance.type') }}</th>
            <th>{{ t('admin.compliance.risk') }}</th>
            <th>{{ t('admin.compliance.status') }}</th>
            <th>{{ t('admin.compliance.kyc.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (alert of alerts; track alert.id) {
            <tr>
              <td>{{ alert.transferReference }}</td>
              <td>{{ alert.type }}</td>
              <td><app-status-badge [value]="alert.riskLevel"/></td>
              <td><app-status-badge [value]="alert.status"/></td>
              <td>
                @if (alert.status === 'OPEN' || alert.status === 'UNDER_REVIEW') {
                  <div class="actions">
                    <button class="btn" type="button" (click)="reviewAlert(alert, 'UNDER_REVIEW')" [disabled]="reviewingAlertId === alert.id">{{ t('admin.compliance.aml.review') }}</button>
                    <button class="btn primary" type="button" (click)="reviewAlert(alert, 'RESOLVED')" [disabled]="reviewingAlertId === alert.id">{{ t('admin.compliance.aml.resolve') }}</button>
                    <button class="btn" type="button" (click)="reviewAlert(alert, 'FALSE_POSITIVE')" [disabled]="reviewingAlertId === alert.id">{{ t('admin.compliance.aml.false_positive') }}</button>
                  </div>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  </div>
</section>`,
  styles: [`
    .summary-grid {
      margin-bottom: 1rem;
    }
    .metric {
      display: grid;
      gap: .35rem;
    }
    .metric span {
      color: var(--muted);
      font-size: .85rem;
    }
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1rem;
      flex-wrap: wrap;
    }
    .card-header h3 {
      margin: 0;
    }
    .warning-box {
      margin-bottom: .75rem;
      border: 1px solid #fde68a;
      background: #fffbeb;
      color: #92400e;
      border-radius: .75rem;
      padding: .75rem;
      font-weight: 700;
    }
    .muted {
      color: var(--muted);
      margin: 0;
    }
    .reason-input {
      min-width: 12rem;
    }
    .actions {
      display: flex;
      gap: .5rem;
      flex-wrap: wrap;
    }
    small {
      display: block;
      margin-top: .3rem;
      color: var(--muted);
    }
  `]
})
export class ComplianceComponent implements OnInit {
  kycDocuments: KycDocumentResponse[] = [];
  alerts: AmlAlertResponse[] = [];
  summary: ComplianceSummaryResponse | null = null;
  rejectionReasons: Record<number, string> = {};

  kycLoading = false;
  reviewingId: number | null = null;
  reviewingAlertId: number | null = null;
  kycWarning = '';
  kycError = '';
  reviewMessage = '';
  reviewError = '';
  amlReviewMessage = '';
  amlError = '';

  constructor(private data: DataService, private i18n: I18nService) {}

  ngOnInit(): void {
    this.loadSummary();
    this.loadKyc();
    this.loadAml();
  }

  loadSummary(): void {
    this.data.complianceSummary().subscribe(summary => this.summary = summary);
  }

  loadKyc(): void {
    this.kycLoading = true;
    this.kycWarning = '';
    this.kycError = '';
    this.reviewMessage = '';
    this.reviewError = '';

    this.data.pendingKycDocuments().subscribe({
      next: result => {
        this.kycDocuments = result.documents;
        this.kycWarning = result.backendUnavailable ? this.t('admin.compliance.kyc.backend_unavailable') : '';
        this.kycLoading = false;
      },
      error: () => {
        this.kycDocuments = [];
        this.kycError = this.t('admin.compliance.kyc.load_error');
        this.kycLoading = false;
      }
    });
  }

  reviewDocument(document: KycDocumentResponse, status: KycReviewDecision): void {
    const rejectionReason = (this.rejectionReasons[document.id] ?? '').trim();
    if (status === 'REJECTED' && !rejectionReason) {
      this.reviewError = this.t('admin.compliance.kyc.rejection_reason_required');
      return;
    }

    const request: KycReviewRequest = status === 'REJECTED'
      ? { status, rejectionReason }
      : { status };

    this.reviewingId = document.id;
    this.reviewMessage = '';
    this.reviewError = '';

    this.data.reviewKycDocument(document.id, request).subscribe({
      next: result => {
        this.kycDocuments = this.kycDocuments.filter(item => item.id !== result.document.id);
        delete this.rejectionReasons[document.id];
        this.reviewMessage = status === 'APPROVED'
          ? this.t('admin.compliance.kyc.approve_success')
          : this.t('admin.compliance.kyc.reject_success');
        this.kycWarning = result.backendUnavailable ? this.t('admin.compliance.kyc.backend_unavailable') : this.kycWarning;
        this.reviewingId = null;
      },
      error: () => {
        this.reviewError = this.t('admin.compliance.kyc.review_error');
        this.reviewingId = null;
      }
    });
  }

  canReject(document: KycDocumentResponse): boolean {
    return !this.isReviewing(document.id) && !!(this.rejectionReasons[document.id] ?? '').trim();
  }

  isReviewing(documentId: number): boolean {
    return this.reviewingId === documentId;
  }

  ownerLabel(document: KycDocumentResponse): string {
    return document.userName
      || document.clientName
      || document.userEmail
      || (document.userId ? `#${document.userId}` : this.t('admin.compliance.kyc.unavailable'));
  }

  formatDate(value: string): string {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat(undefined, { dateStyle: 'short', timeStyle: 'short' }).format(date);
  }

  t(key: string): string {
    return this.i18n.get(key);
  }

  loadAml(): void {
    this.amlError = '';
    this.data.amlAlerts().subscribe({
      next: alerts => this.alerts = alerts,
      error: () => {
        this.alerts = [];
        this.amlError = this.t('admin.compliance.aml.load_error');
      }
    });
  }

  reviewAlert(alert: AmlAlertResponse, status: AmlAlertStatus): void {
    this.reviewingAlertId = alert.id;
    this.amlReviewMessage = '';
    this.amlError = '';
    this.data.reviewAmlAlert(alert.id, { status }).subscribe({
      next: updated => {
        this.alerts = this.alerts.map(item => item.id === updated.id ? updated : item);
        this.amlReviewMessage = this.t('admin.compliance.aml_review_success');
        this.reviewingAlertId = null;
        this.loadSummary();
      },
      error: () => {
        this.amlError = this.t('admin.compliance.aml_review_error');
        this.reviewingAlertId = null;
      }
    });
  }
}
