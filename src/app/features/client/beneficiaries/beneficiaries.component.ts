import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../../core/services/data.service';
import { BeneficiaryRequest, BeneficiaryResponse } from '../../../core/models/transfer.models';
import { IDENTITY_TYPES, IdentityType } from '../../../core/models/enums';

@Component({
  selector: 'app-beneficiaries',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<section class="page">
  <h1 class="page-title">Bénéficiaires</h1>
  @if (errorMsg) { <div class="alert danger">{{ errorMsg }}</div> }
  <div class="card">
    <form class="form-grid" (ngSubmit)="save()">
      <input name="fn" [(ngModel)]="form.firstName" placeholder="Prénom">
      <input name="ln" [(ngModel)]="form.lastName" placeholder="Nom">
      <input name="ph" [(ngModel)]="form.phoneNumber" placeholder="Téléphone">
      <input name="co" type="number" [(ngModel)]="form.countryId">
      <select name="it" [(ngModel)]="form.identityType">
        @for (t of types; track t) { <option [value]="t">{{ t }}</option> }
      </select>
      <input name="idn" [(ngModel)]="form.identityNumber">
      <button class="btn primary">{{ editId ? 'Modifier' : 'Créer' }}</button>
      @if (editId) { <button class="btn" type="button" (click)="cancelEdit()">Annuler</button> }
    </form>
  </div>
  <div class="card table-wrap">
    <table>
      <tr><th>Nom</th><th>Téléphone</th><th>Pays</th><th>Actions</th></tr>
      @for (b of beneficiaries; track b.id) {
        <tr>
          <td>{{ b.fullName }}</td>
          <td>{{ b.phoneNumber }}</td>
          <td>{{ b.country }}</td>
          <td>
            <button class="btn" (click)="edit(b)">Edit</button>
            <button class="btn danger" (click)="remove(b.id)">Delete</button>
          </td>
        </tr>
      }
    </table>
  </div>
</section>`
})
export class BeneficiariesComponent implements OnInit {
  types = IDENTITY_TYPES;
  beneficiaries: BeneficiaryResponse[] = [];
  editId?: number;
  errorMsg = '';
  form: BeneficiaryRequest = { firstName: '', lastName: '', phoneNumber: '', countryId: 2, identityType: 'PASSPORT', identityNumber: '' };

  constructor(private data: DataService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.data.beneficiaries().subscribe({
      next: x => { this.beneficiaries = x; this.errorMsg = ''; },
      error: (err) => {
        console.warn('[Beneficiaries] backend endpoint missing, using fallback mock');
        this.errorMsg = err?.status === 404
          ? 'Endpoint bénéficiaires non disponible côté backend.'
          : 'Impossible de charger les bénéficiaires depuis le backend.';
      }
    });
  }

  save(): void {
    const obs = this.editId
      ? this.data.updateBeneficiary(this.editId, this.form)
      : this.data.createBeneficiary(this.form);
    obs.subscribe({
      next: () => { this.cancelEdit(); this.load(); },
      error: (err) => {
        this.errorMsg = err?.status === 404
          ? 'Endpoint bénéficiaires non disponible côté backend.'
          : 'Erreur lors de la sauvegarde du bénéficiaire.';
      }
    });
  }

  edit(b: BeneficiaryResponse): void {
    this.editId = b.id;
    const [firstName, ...rest] = b.fullName.split(' ');
    this.form = {
      firstName,
      lastName: rest.join(' '),
      phoneNumber: b.phoneNumber,
      countryId: 2,
      identityType: (b.identityType ?? 'PASSPORT') as IdentityType,
      identityNumber: b.identityNumber ?? ''
    };
  }

  cancelEdit(): void {
    this.editId = undefined;
    this.form = { firstName: '', lastName: '', phoneNumber: '', countryId: 2, identityType: 'PASSPORT', identityNumber: '' };
  }

  remove(id: number): void {
    this.data.deleteBeneficiary(id).subscribe({
      next: () => this.load(),
      error: (err) => {
        this.errorMsg = err?.status === 404
          ? 'Endpoint bénéficiaires non disponible côté backend.'
          : 'Erreur lors de la suppression du bénéficiaire.';
      }
    });
  }
}
