import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../../core/services/data.service';
import { I18nService } from '../../../core/services/i18n.service';
import { KycDocumentResponse, KycDocumentUploadRequest } from '../../../core/models/compliance.models';
import { KycDocumentType } from '../../../core/models/enums';
import { UserProfileResponse, UserUpdateRequest } from '../../../core/models/user.models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
<section class="page">
  <h1 class="page-title">Mon profil</h1>
  @if (loading) { <p>Chargement...</p> }
  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  @if (saveMsg) { <div class="alert" [class.success]="saveSuccess" [class.danger]="!saveSuccess">{{ saveMsg }}</div> }
  @if (!loading && profile) {
    <div class="card" style="margin-bottom:1rem">
      <p><b>Email :</b> {{ profile.email }}</p>
      <p><b>Rôle :</b> {{ profile.role }}</p>
    </div>
    <div class="card">
      <form class="form-grid" (ngSubmit)="save()">
        <input [(ngModel)]="form.firstName" name="fn" placeholder="Prénom">
        <input [(ngModel)]="form.lastName" name="ln" placeholder="Nom">
        <input [(ngModel)]="form.phoneNumber" name="tel" placeholder="Téléphone">
        <select [(ngModel)]="form.preferredLanguage" name="lang">
          <option value="FR">FR</option>
          <option value="EN">EN</option>
          <option value="AR">AR</option>
        </select>
        <button class="btn primary" type="submit" [disabled]="saving">
          {{ saving ? 'Enregistrement...' : 'Enregistrer' }}
        </button>
      </form>
    </div>

    <div class="card kyc-card">
      <div class="section-header">
        <h2>{{ t('client.kyc.title') }}</h2>
        <button class="btn" type="button" (click)="loadKycDocuments()" [disabled]="kycLoading">
          {{ kycLoading ? t('client.kyc.loading') : t('client.kyc.refresh') }}
        </button>
      </div>

      @if (kycWarning) { <div class="alert warning">{{ kycWarning }}</div> }
      @if (kycError) { <div class="alert danger">{{ kycError }}</div> }
      @if (kycSuccess) { <div class="alert success">{{ kycSuccess }}</div> }

      <form class="form-grid kyc-form">
        <label>
          <span>{{ t('client.kyc.document_type') }}</span>
          <select name="kycDocumentType" [(ngModel)]="kycForm.documentType">
            @for (type of documentTypes; track type) {
              <option [value]="type">{{ type }}</option>
            }
          </select>
        </label>
        <label>
          <span>{{ t('client.kyc.document_number') }}</span>
          <input name="kycDocumentNumber" [(ngModel)]="kycForm.documentNumber">
        </label>
        <div class="kyc-file-field">
          <span>{{ t('client.kyc.file') }}</span>
          <input
            name="kycFile"
            type="file"
            accept=".pdf,.jpg,.jpeg,.png"
            (change)="onKycFileSelected($event)">
          <small>
            @if (kycFile) {
              {{ t('client.kyc.selected_file') }}: {{ kycFile.name }} ({{ formatFileSize(kycFile.size) }})
            } @else {
              {{ t('client.kyc.no_file_selected') }}
            }
          </small>
          @if (kycFileError) {
            <div class="error-box compact">{{ kycFileError }}</div>
          }
        </div>
        @if (kycUploading) {
          <p class="muted">{{ t('client.kyc.uploading') }}</p>
        }
        <button class="btn primary" type="button" (click)="uploadKycDocument()" [disabled]="!canUploadKyc">
          {{ kycUploading ? t('client.kyc.uploading') : t('client.kyc.upload_document') }}
        </button>
      </form>

      @if (kycLoading) {
        <p class="muted">{{ t('client.kyc.loading') }}</p>
      } @else if (!kycError && kycDocuments.length === 0) {
        <p class="muted">{{ t('client.kyc.empty') }}</p>
      } @else if (kycDocuments.length > 0) {
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>{{ t('client.kyc.id') }}</th>
                <th>{{ t('client.kyc.document_type') }}</th>
                <th>{{ t('client.kyc.status') }}</th>
                <th>{{ t('client.kyc.uploaded_at') }}</th>
                <th>{{ t('client.kyc.rejection_reason') }}</th>
              </tr>
            </thead>
            <tbody>
              @for (document of kycDocuments; track document.id) {
                <tr>
                  <td>#{{ document.id }}</td>
                  <td>{{ document.documentType }}</td>
                  <td><app-status-badge [value]="document.status"/></td>
                  <td>{{ formatDate(document.uploadedAt) }}</td>
                  <td>{{ document.rejectionReason || t('client.kyc.not_applicable') }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  }
</section>`,
  styles: [`
    .kyc-card {
      margin-top: 1rem;
      display: grid;
      gap: 1rem;
    }
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      flex-wrap: wrap;
    }
    .section-header h2 {
      margin: 0;
      font-size: 1.15rem;
    }
    .kyc-form label,
    .kyc-file-field {
      display: grid;
      gap: .35rem;
      font-weight: 700;
    }
    .kyc-form span,
    .kyc-file-field span {
      color: var(--muted);
      font-size: .85rem;
    }
    .kyc-form small {
      color: var(--muted);
      font-weight: 600;
      overflow-wrap: anywhere;
    }
    .muted {
      color: var(--muted);
      margin: 0;
    }
    .compact {
      margin-top: .25rem;
    }
    .warning {
      background: #fffbeb;
      border-color: #fde68a;
      color: #92400e;
    }
  `]
})
export class ProfileComponent implements OnInit {
  readonly documentTypes: KycDocumentType[] = ['CIN_FRONT', 'CIN_BACK', 'PASSPORT', 'PROOF_OF_ADDRESS'];
  private readonly maxKycFileSize = 5 * 1024 * 1024;
  private readonly allowedKycExtensions = ['pdf', 'jpg', 'jpeg', 'png'];

  loading = true;
  saving = false;
  errorMsg = '';
  saveMsg = '';
  saveSuccess = false;
  profile: UserProfileResponse | null = null;
  form: UserUpdateRequest = { firstName: '', lastName: '', phoneNumber: '', preferredLanguage: 'FR' };

  kycDocuments: KycDocumentResponse[] = [];
  kycLoading = false;
  kycUploading = false;
  kycWarning = '';
  kycError = '';
  kycSuccess = '';
  kycFile: File | null = null;
  kycFileError = '';
  private kycFileInput: HTMLInputElement | null = null;
  kycForm: { documentType: KycDocumentType; documentNumber: string } = {
    documentType: 'CIN_FRONT',
    documentNumber: ''
  };

  constructor(private data: DataService, private i18n: I18nService) {}

  ngOnInit(): void {
    this.loadKycDocuments();
    this.data.currentUserProfile().subscribe({
      next: p => {
        this.profile = p;
        this.form = { firstName: p.firstName, lastName: p.lastName, phoneNumber: p.phoneNumber, preferredLanguage: p.preferredLanguage };
        this.loading = false;
      },
      error: () => {
        this.errorMsg = 'Impossible de charger le profil.';
        this.loading = false;
      }
    });
  }

  get canUploadKyc(): boolean {
    return Boolean(
      this.kycForm.documentType &&
      this.kycForm.documentNumber.trim() &&
      this.kycFile &&
      !this.kycFileError &&
      !this.kycUploading
    );
  }

  loadKycDocuments(): void {
    this.kycLoading = true;
    this.kycWarning = '';
    this.kycError = '';

    this.data.clientKycDocuments().subscribe({
      next: result => {
        this.kycDocuments = result.documents;
        this.kycWarning = result.backendUnavailable ? this.t('client.kyc.backend_unavailable') : '';
        this.kycLoading = false;
      },
      error: () => {
        this.kycDocuments = [];
        this.kycError = this.t('client.kyc.load_error');
        this.kycLoading = false;
      }
    });
  }

  onKycFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.kycFileInput = input;
    this.kycFile = null;
    this.kycFileError = '';
    this.kycError = '';
    this.kycSuccess = '';

    if (!file) return;

    const fileValidationError = this.validateKycFile(file);
    if (fileValidationError) {
      this.kycFileError = fileValidationError;
      input.value = '';
      return;
    }

    this.kycFile = file;
  }

  uploadKycDocument(): void {
    const validationError = this.validateKycForm();
    if (validationError) {
      this.kycError = validationError;
      this.kycSuccess = '';
      return;
    }
    if (!this.kycFile) return;

    const request: KycDocumentUploadRequest = {
      file: this.kycFile,
      documentType: this.kycForm.documentType,
      documentNumber: this.kycForm.documentNumber.trim()
    };

    this.kycUploading = true;
    this.kycError = '';
    this.kycSuccess = '';

    this.data.uploadClientKycDocument(request).subscribe({
      next: result => {
        this.kycDocuments = [result.document, ...this.kycDocuments.filter(document => document.id !== result.document.id)];
        this.kycWarning = result.backendUnavailable ? this.t('client.kyc.backend_unavailable') : this.kycWarning;
        this.kycSuccess = this.t('client.kyc.upload_success');
        this.kycForm = { documentType: 'CIN_FRONT', documentNumber: '' };
        this.kycFile = null;
        this.kycFileError = '';
        if (this.kycFileInput) this.kycFileInput.value = '';
        this.kycUploading = false;
        this.loadKycDocuments();
      },
      error: () => {
        this.kycError = this.t('client.kyc.upload_error');
        this.kycUploading = false;
      }
    });
  }

  save(): void {
    if (!this.profile) return;
    this.saving = true;
    this.saveMsg = '';
    this.data.updateUserProfile(this.profile.id, this.form).subscribe({
      next: updated => {
        this.profile = updated;
        this.saveMsg = 'Profil mis à jour avec succès.';
        this.saveSuccess = true;
        this.saving = false;
      },
      error: (err) => {
        const status = err?.status;
        const backendMessage = typeof err?.error === 'object' ? err.error?.message : '';
        this.saveMsg = backendMessage
          ? `Erreur backend ${status ?? ''} : ${backendMessage}`
          : `Erreur backend ${status ?? '?'} lors de la mise à jour du profil.`;
        this.saveSuccess = false;
        this.saving = false;
      }
    });
  }

  formatDate(value: string): string {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat(undefined, { dateStyle: 'short', timeStyle: 'short' }).format(date);
  }

  formatFileSize(size: number): string {
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
    return `${(size / (1024 * 1024)).toFixed(2)} MB`;
  }

  t(key: string): string {
    return this.i18n.get(key);
  }

  private validateKycForm(): string {
    const metadataValidationError = this.validateKycMetadata();
    if (metadataValidationError) return metadataValidationError;
    if (!this.kycFile) return this.t('client.kyc.validation_file');

    const fileValidationError = this.validateKycFile(this.kycFile);
    if (fileValidationError) return fileValidationError;

    return '';
  }

  private validateKycMetadata(): string {
    if (!this.kycForm.documentType) return this.t('client.kyc.validation_document_type');
    if (!this.kycForm.documentNumber.trim()) return this.t('client.kyc.validation_document_number');

    return '';
  }

  private validateKycFile(file: File): string {
    const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
    if (!this.allowedKycExtensions.includes(extension)) return this.t('client.kyc.validation_file_type');
    if (file.size > this.maxKycFileSize) return this.t('client.kyc.validation_file_size');

    return '';
  }
}
