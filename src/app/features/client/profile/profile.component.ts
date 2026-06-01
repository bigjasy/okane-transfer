import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../../core/services/data.service';
import { UserProfileResponse, UserUpdateRequest } from '../../../core/models/user.models';
import { Language } from '../../../core/models/enums';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
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
  }
</section>`
})
export class ProfileComponent implements OnInit {
  loading = true;
  saving = false;
  errorMsg = '';
  saveMsg = '';
  saveSuccess = false;
  profile: UserProfileResponse | null = null;
  form: UserUpdateRequest = { firstName: '', lastName: '', phoneNumber: '', preferredLanguage: 'FR' };

  constructor(private data: DataService) {}

  ngOnInit(): void {
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
        this.saveMsg = status === 404
          ? 'Endpoint backend de mise à jour non disponible.'
          : 'Erreur lors de la mise à jour du profil.';
        this.saveSuccess = false;
        this.saving = false;
      }
    });
  }
}
