import { Component, OnInit } from '@angular/core';
import { AuditLogResponse } from '../../../core/models/notification.models';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';

@Component({
  selector: 'app-audit',
  standalone: true,
  template: `
    <section class="page audit-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">{{ t('admin.audit.title') }}</h1>
          <p class="muted">{{ t('admin.audit.subtitle') }}</p>
        </div>
      </div>

      @if (backendUnavailable) {
        <div class="alert warning">{{ t('admin.audit.backend_unavailable') }}</div>
      }

      @if (errorMessage) {
        <div class="alert error">{{ errorMessage }}</div>
      }

      <div class="card audit-toolbar">
        <label>
          <span>{{ t('admin.audit.filter_action') }}</span>
          <input
            type="search"
            [placeholder]="t('admin.audit.filter_action_placeholder')"
            [value]="actionFilter"
            (input)="setActionFilter($event)"
          />
        </label>
      </div>

      <div class="card table-wrap">
        @if (loading) {
          <p class="state">{{ t('admin.audit.loading') }}</p>
        } @else if (logs.length === 0) {
          <p class="state">{{ t('admin.audit.empty') }}</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th>{{ t('admin.audit.id') }}</th>
                <th>{{ t('admin.audit.action') }}</th>
                <th>{{ t('admin.audit.actor_user_id') }}</th>
                <th>{{ t('admin.audit.actor') }}</th>
                <th>{{ t('admin.audit.entity') }}</th>
                <th>{{ t('admin.audit.ip') }}</th>
                <th>{{ t('admin.audit.user_agent') }}</th>
                <th>{{ t('admin.audit.details') }}</th>
                <th>{{ t('admin.audit.date') }}</th>
                <th>{{ t('admin.audit.actions') }}</th>
              </tr>
            </thead>
            <tbody>
              @for (log of logs; track log.id) {
                <tr>
                  <td>#{{ log.id }}</td>
                  <td>{{ log.action }}</td>
                  <td>{{ valueOrDash(log.actorUserId) }}</td>
                  <td>{{ valueOrDash(log.actorEmail) }}</td>
                  <td>{{ formatEntity(log) }}</td>
                  <td>{{ valueOrDash(log.ipAddress) }}</td>
                  <td>{{ shorten(log.userAgent, 34) }}</td>
                  <td>{{ shorten(log.detailsJson, 44) }}</td>
                  <td>{{ formatDate(log.createdAt) }}</td>
                  <td>
                    <button type="button" class="link-button" (click)="viewDetails(log)">
                      {{ t('admin.audit.view_details') }}
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      </div>

      <div class="pagination">
        <button type="button" (click)="previousPage()" [disabled]="loading || page === 0">
          {{ t('admin.audit.previous') }}
        </button>
        <span>{{ t('admin.audit.page') }} {{ page + 1 }} {{ t('admin.audit.of') }} {{ displayTotalPages }}</span>
        <button type="button" (click)="nextPage()" [disabled]="loading || !hasNextPage">
          {{ t('admin.audit.next') }}
        </button>
        <span class="muted">{{ totalElements }} {{ t('admin.audit.total') }}</span>
      </div>

      @if (selectedLog) {
        <div class="card audit-detail">
          <div class="detail-header">
            <h2>{{ t('admin.audit.details_title') }} #{{ selectedLog.id }}</h2>
            <button type="button" class="link-button" (click)="closeDetails()">
              {{ t('admin.audit.close_details') }}
            </button>
          </div>

          @if (detailLoading) {
            <p class="state">{{ t('admin.audit.detail_loading') }}</p>
          } @else {
            @if (detailErrorMessage) {
              <div class="alert error">{{ detailErrorMessage }}</div>
            }

            <dl>
              <div>
                <dt>{{ t('admin.audit.action') }}</dt>
                <dd>{{ selectedLog.action }}</dd>
              </div>
              <div>
                <dt>{{ t('admin.audit.actor_user_id') }}</dt>
                <dd>{{ valueOrDash(selectedLog.actorUserId) }}</dd>
              </div>
              <div>
                <dt>{{ t('admin.audit.actor') }}</dt>
                <dd>{{ valueOrDash(selectedLog.actorEmail) }}</dd>
              </div>
              <div>
                <dt>{{ t('admin.audit.user_agent') }}</dt>
                <dd>{{ valueOrDash(selectedLog.userAgent) }}</dd>
              </div>
              <div>
                <dt>{{ t('admin.audit.date') }}</dt>
                <dd>{{ formatDate(selectedLog.createdAt) }}</dd>
              </div>
            </dl>

            <pre>{{ formatDetails(selectedLog.detailsJson) }}</pre>
          }
        </div>
      }
    </section>
  `,
  styles: [`
    .audit-page {
      display: grid;
      gap: 16px;
    }

    .page-header,
    .detail-header,
    .pagination {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      flex-wrap: wrap;
    }

    .muted {
      color: #64748b;
      margin: 4px 0 0;
    }

    .alert {
      border-radius: 6px;
      padding: 12px 14px;
      font-weight: 600;
    }

    .warning {
      background: #fff7ed;
      color: #9a3412;
      border: 1px solid #fed7aa;
    }

    .error {
      background: #fef2f2;
      color: #991b1b;
      border: 1px solid #fecaca;
    }

    .audit-toolbar label {
      display: grid;
      gap: 6px;
      max-width: 360px;
      font-weight: 600;
    }

    .audit-toolbar input {
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      padding: 10px 12px;
      font: inherit;
    }

    .state {
      margin: 0;
      padding: 16px;
      color: #475569;
    }

    td {
      vertical-align: top;
    }

    .link-button {
      background: transparent;
      border: 0;
      color: #2563eb;
      cursor: pointer;
      font: inherit;
      font-weight: 700;
      padding: 0;
    }

    .link-button:hover {
      text-decoration: underline;
    }

    .pagination button {
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      background: #fff;
      cursor: pointer;
      font: inherit;
      padding: 8px 12px;
    }

    .pagination button:disabled {
      cursor: not-allowed;
      opacity: .5;
    }

    .audit-detail h2 {
      font-size: 1.1rem;
      margin: 0;
    }

    .audit-detail dl {
      display: grid;
      gap: 10px;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      margin: 16px 0;
    }

    .audit-detail dt {
      color: #64748b;
      font-size: .82rem;
      font-weight: 700;
    }

    .audit-detail dd {
      margin: 4px 0 0;
    }

    .audit-detail pre {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      margin: 0;
      max-height: 320px;
      overflow: auto;
      padding: 12px;
      white-space: pre-wrap;
      word-break: break-word;
    }
  `]
})
export class AuditComponent implements OnInit {
  logs: AuditLogResponse[] = [];
  selectedLog: AuditLogResponse | null = null;
  loading = false;
  detailLoading = false;
  backendUnavailable = false;
  errorMessage = '';
  detailErrorMessage = '';
  actionFilter = '';
  page = 0;
  size = 20;
  totalElements = 0;
  totalPages = 0;

  constructor(private readonly data: DataService, private readonly i18n: I18nService) {}

  ngOnInit(): void {
    this.loadLogs();
  }

  get filteredLogs(): AuditLogResponse[] {
    return this.logs;
  }

  get hasNextPage(): boolean {
    return this.totalPages > 0 && this.page + 1 < this.totalPages;
  }

  get displayTotalPages(): number {
    return Math.max(this.totalPages, 1);
  }

  loadLogs(page = this.page): void {
    this.loading = true;
    this.errorMessage = '';
    this.data.auditLogs(page, this.size, this.actionFilter).subscribe({
      next: result => {
        this.logs = result.logs;
        this.page = result.page;
        this.size = result.size;
        this.totalElements = result.totalElements;
        this.totalPages = result.totalPages;
        this.backendUnavailable = result.backendUnavailable;
        this.loading = false;
      },
      error: () => {
        this.logs = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.errorMessage = this.t('admin.audit.load_error');
        this.loading = false;
      }
    });
  }

  previousPage(): void {
    if (this.page === 0) return;
    this.loadLogs(this.page - 1);
  }

  nextPage(): void {
    if (!this.hasNextPage) return;
    this.loadLogs(this.page + 1);
  }

  viewDetails(log: AuditLogResponse): void {
    this.selectedLog = log;
    this.detailErrorMessage = '';
    this.detailLoading = true;
    this.data.auditLog(log.id).subscribe({
      next: result => {
        this.selectedLog = result.log;
        this.backendUnavailable = this.backendUnavailable || result.backendUnavailable;
        this.detailLoading = false;
      },
      error: () => {
        this.detailErrorMessage = this.t('admin.audit.detail_error');
        this.detailLoading = false;
      }
    });
  }

  closeDetails(): void {
    this.selectedLog = null;
    this.detailErrorMessage = '';
  }

  setActionFilter(event: Event): void {
    this.actionFilter = event.target instanceof HTMLInputElement ? event.target.value : '';
    this.page = 0;
    this.loadLogs(0);
  }

  formatEntity(log: AuditLogResponse): string {
    const entityType = log.entityType ?? this.t('admin.audit.not_available');
    return log.entityId ? `${entityType} #${log.entityId}` : entityType;
  }

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
  }

  formatDetails(value?: string): string {
    if (!value) return this.t('admin.audit.not_available');
    try {
      return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
      return value;
    }
  }

  shorten(value: string | undefined, maxLength: number): string {
    const text = this.valueOrDash(value);
    return text.length > maxLength ? `${text.slice(0, maxLength - 1)}...` : text;
  }

  valueOrDash(value: string | number | undefined | null): string {
    return value === undefined || value === null || value === '' ? this.t('admin.audit.not_available') : String(value);
  }

  t(key: string): string {
    return this.i18n.get(key);
  }
}
