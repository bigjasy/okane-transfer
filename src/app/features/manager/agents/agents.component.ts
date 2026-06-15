import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../../core/services/data.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { I18nService } from '../../../core/services/i18n.service';
import { UserSummaryResponse } from '../../../core/models/user.models';
import { AgencyResponse } from '../../../core/models/agency.models';

@Component({
  selector: 'app-agents',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <h1 class="page-title">{{ t('manager.agents.title') }}</h1>
  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (successMsg) { <div class="alert success">{{ successMsg }}</div> }

  <div class="card assign-card">
    <label>{{ t('manager.agents.assign_to_agency') }}
      <select [(ngModel)]="assignAgentId" name="agent">
        <option [ngValue]="null">{{ t('manager.agents.select_agent') }}</option>
        @for (a of agents; track a.id) {
          <option [ngValue]="a.id">{{ a.fullName }} — {{ a.agencyName || t('manager.agents.no_agency') }}</option>
        }
      </select>
    </label>
    <label>{{ t('manager.agents.agency') }}
      <select [(ngModel)]="assignAgencyId" name="agency">
        @for (ag of agencies; track ag.id) {
          <option [ngValue]="ag.id">{{ ag.name }} ({{ ag.code }})</option>
        }
      </select>
    </label>
    <button class="btn primary" type="button" (click)="assign()" [disabled]="!assignAgentId">{{ t('manager.agents.assign') }}</button>
  </div>

  <div class="card table-wrap">
    <table>
      <thead>
        <tr>
          <th>{{ t('manager.agents.name') }}</th>
          <th>{{ t('manager.agents.email') }}</th>
          <th>{{ t('manager.agents.agency') }}</th>
          <th>{{ t('manager.agents.status') }}</th>
        </tr>
      </thead>
      <tbody>
        @for (a of agents; track a.id) {
          <tr>
            <td>{{ a.fullName }}</td>
            <td>{{ a.email }}</td>
            <td>{{ a.agencyName || '—' }}</td>
            <td><app-status-badge [value]="a.status"/></td>
          </tr>
        }
      </tbody>
    </table>
  </div>
</section>`,
  styles: [`
    .assign-card { display: grid; gap: .75rem; margin-bottom: 1rem; max-width: 640px; }
    .assign-card label { display: grid; gap: .35rem; font-weight: 600; }
    .assign-card select { padding: .5rem; }
    .alert { padding: .75rem 1rem; border-radius: .75rem; margin-bottom: 1rem; }
    .alert.danger { background: #fee2e2; color: #991b1b; }
    .alert.success { background: #dcfce7; color: #166534; }
  `]
})
export class AgentsComponent implements OnInit {
  agents: UserSummaryResponse[] = [];
  agencies: AgencyResponse[] = [];
  assignAgentId: number | null = null;
  assignAgencyId: number | null = null;
  errorMsg = '';
  successMsg = '';

  constructor(private data: DataService, public i18n: I18nService) {}

  ngOnInit(): void {
    this.data.users(0, 200).subscribe(r => this.agents = r.content.filter(x => x.role === 'ROLE_AGENT'));
    this.data.agencies(0, 100).subscribe(r => {
      this.agencies = r.content;
      if (this.agencies.length) this.assignAgencyId = this.agencies[0].id;
    });
  }

  assign(): void {
    if (!this.assignAgentId || !this.assignAgencyId) return;
    this.data.assignAgentToAgency(this.assignAgencyId, this.assignAgentId).subscribe({
      next: () => {
        this.successMsg = this.t('manager.agents.assign_success');
        this.data.users(0, 200).subscribe(r => this.agents = r.content.filter(x => x.role === 'ROLE_AGENT'));
      },
      error: err => this.errorMsg = err?.error?.message || this.t('manager.agents.assign_error')
    });
  }

  t(key: string): string { return this.i18n.get(key); }
}
