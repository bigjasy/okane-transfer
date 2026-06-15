import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../../core/services/data.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { I18nService } from '../../../core/services/i18n.service';
import { CashRegisterResponse } from '../../../core/models/finance.models';
import { AgencyResponse } from '../../../core/models/agency.models';

@Component({
  selector: 'app-cash-registers',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <h1 class="page-title">{{ t('manager.cash_registers.title') }}</h1>
  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }

  <div class="card filter-card">
    <label>{{ t('manager.cash_registers.agency') }}
      <select [(ngModel)]="selectedAgencyId" name="agency" (ngModelChange)="loadAgencyRegisters()">
        @for (ag of agencies; track ag.id) {
          <option [ngValue]="ag.id">{{ ag.name }} ({{ ag.code }})</option>
        }
      </select>
    </label>
  </div>

  <div class="card">
    <h3>{{ t('manager.cash_registers.my_current') }}</h3>
    @if (current) {
      <p>{{ t('manager.cash_registers.current_balance') }}: <b>{{ current.currentBalance }} {{ current.currencyCode }}</b></p>
      <app-status-badge [value]="current.status"/>
    } @else {
      <p class="muted">{{ t('manager.cash_registers.no_open') }}</p>
    }
  </div>

  <div class="card table-wrap">
    <h3>{{ t('manager.cash_registers.agency_registers') }}</h3>
    @if (loading) { <p>{{ t('manager.cash_registers.loading') }}</p> }
    @else if (registers.length === 0) { <p class="muted">{{ t('manager.cash_registers.empty') }}</p> }
    @else {
      <table>
        <thead>
          <tr>
            <th>{{ t('manager.cash_registers.agent') }}</th>
            <th>{{ t('manager.cash_registers.currency') }}</th>
            <th>{{ t('manager.cash_registers.opening') }}</th>
            <th>{{ t('manager.cash_registers.balance') }}</th>
            <th>{{ t('manager.cash_registers.status') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (r of registers; track r.id) {
            <tr>
              <td>{{ r.agentName }}</td>
              <td>{{ r.currencyCode }}</td>
              <td>{{ r.openingBalance }}</td>
              <td>{{ r.currentBalance }}</td>
              <td><app-status-badge [value]="r.status"/></td>
            </tr>
          }
        </tbody>
      </table>
    }
  </div>
</section>`,
  styles: [`
    .filter-card label { display: grid; gap: .35rem; font-weight: 600; max-width: 400px; }
    .filter-card select { padding: .5rem; }
    .alert.danger { background: #fee2e2; color: #991b1b; padding: .75rem 1rem; border-radius: .75rem; }
    .muted { color: #64748b; }
  `]
})
export class CashRegistersComponent implements OnInit {
  agencies: AgencyResponse[] = [];
  registers: CashRegisterResponse[] = [];
  current: CashRegisterResponse | null = null;
  selectedAgencyId: number | null = null;
  loading = false;
  errorMsg = '';

  constructor(private data: DataService, public i18n: I18nService) {}

  ngOnInit(): void {
    this.data.currentCash().subscribe({
      next: x => this.current = x,
      error: () => this.current = null
    });
    this.data.agencies(0, 100).subscribe(r => {
      this.agencies = r.content;
      if (this.agencies.length) {
        this.selectedAgencyId = this.agencies[0].id;
        this.loadAgencyRegisters();
      }
    });
  }

  loadAgencyRegisters(): void {
    if (!this.selectedAgencyId) return;
    this.loading = true;
    this.errorMsg = '';
    this.data.agencyCashRegisters(this.selectedAgencyId).subscribe({
      next: rows => { this.registers = rows; this.loading = false; },
      error: err => {
        this.loading = false;
        this.errorMsg = err?.error?.message || this.t('manager.cash_registers.load_error');
      }
    });
  }

  t(key: string): string { return this.i18n.get(key); }
}
