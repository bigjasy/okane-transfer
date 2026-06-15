import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { DataService } from '../../core/services/data.service';
import { I18nService } from '../../core/services/i18n.service';
import { NotificationResponse } from '../../core/models/notification.models';
import { NotificationPreferencesResponse } from '../../core/models/notification.models';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('notifications.title') }}</h1>
      <p class="page-subtitle">{{ t('notifications.subtitle') }}</p>
    </div>
    <button class="btn" type="button" (click)="load()" [disabled]="loading">
      {{ loading ? t('notifications.loading') : t('notifications.refresh') }}
    </button>
  </div>

  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (successMsg) { <div class="alert success">{{ successMsg }}</div> }

  <div class="grid cols-2">
    <div class="card">
      <h3>{{ t('notifications.inbox_title') }}</h3>
      @if (loading) {
        <p>{{ t('notifications.loading') }}</p>
      } @else if (items.length === 0) {
        <p class="muted">{{ t('notifications.empty') }}</p>
      } @else {
        <div class="notification-list">
          @for (item of items; track item.id) {
            <article class="notification-item" [class.read]="item.status === 'READ'">
              <div class="notification-head">
                <strong>{{ item.title }}</strong>
                <app-status-badge [value]="item.status"/>
              </div>
              <p>{{ item.message }}</p>
              <div class="notification-meta">
                <span>{{ item.channel }}</span>
                <span>{{ formatDate(item.createdAt) }}</span>
                @if (item.status !== 'READ') {
                  <button class="btn link" type="button" (click)="markRead(item)">
                    {{ t('notifications.mark_read') }}
                  </button>
                }
              </div>
            </article>
          }
        </div>
      }
    </div>

    <div class="card">
      <h3>{{ t('notifications.preferences_title') }}</h3>
      @if (preferences) {
        <form class="form-grid" (ngSubmit)="savePreferences()">
          <label class="checkbox-row">
            <input type="checkbox" name="email" [(ngModel)]="preferences.email">
            <span>{{ t('notifications.pref_email') }}</span>
          </label>
          <label class="checkbox-row">
            <input type="checkbox" name="sms" [(ngModel)]="preferences.sms">
            <span>{{ t('notifications.pref_sms') }}</span>
          </label>
          <label class="checkbox-row">
            <input type="checkbox" name="push" [(ngModel)]="preferences.push">
            <span>{{ t('notifications.pref_push') }}</span>
          </label>
          <button class="btn primary" type="submit" [disabled]="savingPreferences">
            {{ savingPreferences ? t('notifications.saving') : t('notifications.save_preferences') }}
          </button>
        </form>
      } @else {
        <p class="muted">{{ t('notifications.loading') }}</p>
      }
    </div>
  </div>
</section>
`,
  styles: [`
    .notification-list { display: grid; gap: .75rem; }
    .notification-item { border: 1px solid #e2e8f0; border-radius: .75rem; padding: .9rem; background: #fff; }
    .notification-item.read { opacity: .75; }
    .notification-head { display: flex; justify-content: space-between; gap: .75rem; align-items: center; margin-bottom: .35rem; }
    .notification-meta { display: flex; flex-wrap: wrap; gap: .75rem; align-items: center; margin-top: .5rem; color: #64748b; font-size: .9rem; }
    .checkbox-row { display: flex; align-items: center; gap: .5rem; }
    .btn.link { background: transparent; border: none; color: #1d4ed8; padding: 0; }
    .alert { margin-bottom: 1rem; padding: .75rem 1rem; border-radius: .75rem; }
    .alert.danger { background: #fee2e2; color: #991b1b; }
    .alert.success { background: #dcfce7; color: #166534; }
    .muted { color: #64748b; }
  `]
})
export class NotificationsComponent implements OnInit {
  items: NotificationResponse[] = [];
  preferences: NotificationPreferencesResponse | null = null;
  loading = false;
  savingPreferences = false;
  errorMsg = '';
  successMsg = '';

  constructor(private data: DataService, public i18n: I18nService) {}

  ngOnInit(): void {
    this.load();
    this.loadPreferences();
  }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.data.notifications(0, 50).subscribe({
      next: items => {
        this.items = items;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMsg = `${this.t('notifications.load_error')} (${err?.status ?? '?'})`;
      }
    });
  }

  loadPreferences(): void {
    this.data.notificationPreferences().subscribe({
      next: prefs => this.preferences = { ...prefs },
      error: () => {
        this.preferences = { email: true, sms: false, push: false };
      }
    });
  }

  markRead(item: NotificationResponse): void {
    this.data.markNotificationRead(item.id).subscribe({
      next: updated => {
        this.items = this.items.map(n => n.id === updated.id ? updated : n);
      },
      error: () => {
        this.errorMsg = this.t('notifications.read_error');
      }
    });
  }

  savePreferences(): void {
    if (!this.preferences) return;
    this.savingPreferences = true;
    this.successMsg = '';
    this.data.updateNotificationPreferences(this.preferences).subscribe({
      next: prefs => {
        this.preferences = prefs;
        this.savingPreferences = false;
        this.successMsg = this.t('notifications.preferences_saved');
      },
      error: () => {
        this.savingPreferences = false;
        this.errorMsg = this.t('notifications.preferences_error');
      }
    });
  }

  formatDate(value: string): string {
    return new Date(value).toLocaleString();
  }

  t(key: string): string {
    return this.i18n.get(key);
  }
}
