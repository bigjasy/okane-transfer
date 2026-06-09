import { Component, Input } from '@angular/core';
@Component({ selector: 'app-status-badge', standalone: true, template: `<span class="badge {{ className }}">{{ value }}</span>` })
export class StatusBadgeComponent {
  @Input({ required: true }) value!: string;
  get className(): string { const v = (this.value || '').toUpperCase(); if (['ACTIVE','OPEN','PAID','AVAILABLE','APPROVED','RESOLVED','CONFIRMED','RECONCILED','SENT'].includes(v)) return 'success'; if (['SUSPENDED','DISABLED','CANCELLED','REJECTED','FAILED','BLOCKED_AML','CRITICAL'].includes(v)) return 'danger'; if (['PENDING','PENDING_PAYMENT','UNDER_REVIEW','MEDIUM','NOT_RECONCILED'].includes(v)) return 'warning'; return 'info'; }
}
