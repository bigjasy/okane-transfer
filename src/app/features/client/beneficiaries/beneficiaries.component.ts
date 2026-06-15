import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { CountryResponse } from '../../../core/models/referential.models';
import { BeneficiaryRequest, BeneficiaryResponse } from '../../../core/models/transfer.models';
import { IDENTITY_TYPES, IdentityType } from '../../../core/models/enums';

interface BeneficiaryForm {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  countryId: number | null;
  identityType: IdentityType;
  identityNumber: string;
}

@Component({
  selector: 'app-beneficiaries',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<section class="page">
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ t('client.beneficiaries.title') }}</h1>
      <p class="page-subtitle">{{ t('client.beneficiaries.subtitle') }}</p>
    </div>
  </div>

  @if (backendUnavailable) { <div class="alert warning">{{ t('client.beneficiaries.backend_unavailable') }}</div> }
  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (successMsg) { <div class="alert success">{{ successMsg }}</div> }

  <div class="card">
    <h3>{{ editId ? t('client.beneficiaries.edit_title') : t('client.beneficiaries.create_title') }}</h3>
    @if (editId) {
      <p class="muted">{{ t('client.beneficiaries.edit_identity_hint') }}</p>
    }
    <form class="form-grid" (ngSubmit)="save()">
      <label class="field">
        <span>{{ t('client.beneficiaries.first_name') }}</span>
        <input name="fn" [(ngModel)]="form.firstName" autocomplete="given-name">
      </label>
      <label class="field">
        <span>{{ t('client.beneficiaries.last_name') }}</span>
        <input name="ln" [(ngModel)]="form.lastName" autocomplete="family-name">
      </label>
      <label class="field">
        <span>{{ t('client.beneficiaries.phone') }}</span>
        <input name="ph" [(ngModel)]="form.phoneNumber" type="tel" autocomplete="tel">
      </label>
      <label class="field">
        <span>{{ t('client.beneficiaries.country') }}</span>
        <select name="co" [(ngModel)]="form.countryId">
          <option [ngValue]="null">{{ countriesLoading ? t('client.beneficiaries.loading_countries') : t('client.beneficiaries.select_country') }}</option>
          @for (country of countries; track country.id) {
            <option [ngValue]="country.id">{{ country.name }}</option>
          }
        </select>
      </label>
      <label class="field">
        <span>{{ t('client.beneficiaries.identity_type') }}</span>
        <select name="it" [(ngModel)]="form.identityType">
          @for (type of types; track type) {
            <option [value]="type">{{ type }}</option>
          }
        </select>
      </label>
      <label class="field">
        <span>{{ t('client.beneficiaries.identity_number') }}</span>
        <input name="idn" [(ngModel)]="form.identityNumber">
      </label>
      <div class="form-actions">
        <button class="btn primary" type="submit" [disabled]="saving">
          {{ saving ? t('client.beneficiaries.saving') : (editId ? t('client.beneficiaries.update') : t('client.beneficiaries.create')) }}
        </button>
        @if (editId) {
          <button class="btn" type="button" (click)="cancelEdit()" [disabled]="saving">{{ t('client.beneficiaries.cancel') }}</button>
        }
      </div>
    </form>
  </div>

  <div class="card table-wrap">
    @if (loading) {
      <p class="muted">{{ t('client.beneficiaries.loading') }}</p>
    } @else if (!errorMsg && beneficiaries.length === 0) {
      <p class="muted">{{ t('client.beneficiaries.empty') }}</p>
    } @else if (beneficiaries.length > 0) {
      <table>
        <thead>
          <tr>
            <th>{{ t('client.beneficiaries.name') }}</th>
            <th>{{ t('client.beneficiaries.phone') }}</th>
            <th>{{ t('client.beneficiaries.country') }}</th>
            <th>{{ t('client.beneficiaries.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          @for (beneficiary of beneficiaries; track beneficiary.id) {
            <tr>
              <td>{{ beneficiary.fullName }}</td>
              <td>{{ beneficiary.phoneNumber }}</td>
              <td>{{ beneficiary.country }}</td>
              <td class="row-actions">
                <button class="btn" type="button" (click)="edit(beneficiary)" [disabled]="saving || deletingId === beneficiary.id">{{ t('client.beneficiaries.edit') }}</button>
                <button class="btn danger" type="button" (click)="remove(beneficiary.id)" [disabled]="saving || deletingId === beneficiary.id">
                  {{ deletingId === beneficiary.id ? t('client.beneficiaries.deleting') : t('client.beneficiaries.delete') }}
                </button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  </div>
</section>`,
  styles: [`
    .card h3 {
      margin: 0 0 1rem;
    }
    .field span {
      color: var(--muted);
      font-size: .85rem;
      font-weight: 700;
    }
    .form-actions {
      display: flex;
      align-items: end;
      gap: .75rem;
      flex-wrap: wrap;
    }
    .row-actions {
      display: flex;
      gap: .5rem;
    }
    .muted {
      color: var(--muted);
      margin: 0 0 1rem;
    }
    .table-wrap .muted {
      margin: 0;
    }
    .warning {
      background: #fffbeb;
      border-color: #fde68a;
      color: #92400e;
    }
  `]
})
export class BeneficiariesComponent implements OnInit {
  readonly types = IDENTITY_TYPES;

  beneficiaries: BeneficiaryResponse[] = [];
  countries: CountryResponse[] = [];
  editId?: number;
  deletingId?: number;
  loading = false;
  countriesLoading = false;
  saving = false;
  backendUnavailable = false;
  errorMsg = '';
  successMsg = '';
  form: BeneficiaryForm = this.emptyForm();

  constructor(private data: DataService, private i18n: I18nService) {}

  ngOnInit(): void {
    this.loadCountries();
    this.load();
  }

  load(preserveBackendUnavailable = false): void {
    this.loading = true;
    this.errorMsg = '';
    if (!preserveBackendUnavailable) this.backendUnavailable = false;

    this.data.beneficiaries().subscribe({
      next: result => {
        this.beneficiaries = result.beneficiaries;
        this.backendUnavailable = result.backendUnavailable || preserveBackendUnavailable;
        this.loading = false;
      },
      error: () => {
        this.beneficiaries = [];
        this.errorMsg = this.t('client.beneficiaries.load_error');
        this.loading = false;
      }
    });
  }

  loadCountries(): void {
    this.countriesLoading = true;
    this.data.countries().subscribe({
      next: countries => {
        this.countries = countries.filter(country => country.active);
        this.countriesLoading = false;
      },
      error: () => {
        this.errorMsg = this.t('client.beneficiaries.countries_error');
        this.countriesLoading = false;
      }
    });
  }

  save(): void {
    const validationError = this.validateForm();
    if (validationError) {
      this.errorMsg = validationError;
      this.successMsg = '';
      return;
    }

    const request = this.toRequest();
    this.saving = true;
    this.errorMsg = '';
    this.successMsg = '';

    const action = this.editId
      ? this.data.updateBeneficiary(this.editId, request)
      : this.data.createBeneficiary(request);

    action.subscribe({
      next: result => {
        this.backendUnavailable = result.backendUnavailable || this.backendUnavailable;
        this.successMsg = this.editId ? this.t('client.beneficiaries.update_success') : this.t('client.beneficiaries.create_success');
        this.cancelEdit();
        this.saving = false;
        this.load(result.backendUnavailable);
      },
      error: () => {
        this.errorMsg = this.editId ? this.t('client.beneficiaries.update_error') : this.t('client.beneficiaries.create_error');
        this.saving = false;
      }
    });
  }

  edit(beneficiary: BeneficiaryResponse): void {
    const [firstName, ...lastNameParts] = beneficiary.fullName.trim().split(/\s+/);
    this.editId = beneficiary.id;
    this.errorMsg = '';
    this.successMsg = '';
    this.form = {
      firstName: firstName ?? '',
      lastName: lastNameParts.join(' '),
      phoneNumber: beneficiary.phoneNumber,
      countryId: this.countryIdFromName(beneficiary.country),
      identityType: 'PASSPORT',
      identityNumber: ''
    };
  }

  cancelEdit(): void {
    this.editId = undefined;
    this.form = this.emptyForm();
  }

  remove(id: number): void {
    this.deletingId = id;
    this.errorMsg = '';
    this.successMsg = '';

    this.data.deleteBeneficiary(id).subscribe({
      next: result => {
        this.backendUnavailable = result.backendUnavailable || this.backendUnavailable;
        this.beneficiaries = this.beneficiaries.filter(beneficiary => beneficiary.id !== id);
        this.successMsg = this.t('client.beneficiaries.delete_success');
        this.deletingId = undefined;
      },
      error: () => {
        this.errorMsg = this.t('client.beneficiaries.delete_error');
        this.deletingId = undefined;
      }
    });
  }

  t(key: string): string {
    return this.i18n.get(key);
  }

  private emptyForm(): BeneficiaryForm {
    return { firstName: '', lastName: '', phoneNumber: '', countryId: null, identityType: 'PASSPORT', identityNumber: '' };
  }

  private validateForm(): string {
    if (!this.form.firstName.trim()) return this.t('client.beneficiaries.validation_first_name');
    if (!this.form.lastName.trim()) return this.t('client.beneficiaries.validation_last_name');
    if (!this.form.phoneNumber.trim()) return this.t('client.beneficiaries.validation_phone');
    if (!this.form.countryId) return this.t('client.beneficiaries.validation_country');
    if (!this.form.identityType) return this.t('client.beneficiaries.validation_identity_type');
    if (!this.form.identityNumber.trim()) return this.t('client.beneficiaries.validation_identity_number');

    return '';
  }

  private toRequest(): BeneficiaryRequest {
    return {
      firstName: this.form.firstName.trim(),
      lastName: this.form.lastName.trim(),
      phoneNumber: this.form.phoneNumber.trim(),
      countryId: this.form.countryId ?? 0,
      identityType: this.form.identityType,
      identityNumber: this.form.identityNumber.trim()
    };
  }

  private countryIdFromName(countryName: string): number | null {
    const normalizedName = this.normalize(countryName);
    return this.countries.find(country => this.normalize(country.name) === normalizedName)?.id ?? null;
  }

  private normalize(value: string): string {
    return value.trim().toLocaleLowerCase();
  }
}
