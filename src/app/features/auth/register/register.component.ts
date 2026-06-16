import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DataService } from '../../../core/services/data.service';
import { CountryResponse } from '../../../core/models/referential.models';
import { ClientRegisterRequest } from '../../../core/models/auth.models';
@Component({ selector: 'app-register', standalone: true, imports: [FormsModule, RouterLink], template: `
<div class="card reg"><h1>Inscription client</h1>@if(done){<div class="ok-box">Compte créé avec succès.</div>}
<form (ngSubmit)="submit()" class="form-grid">
<label class="field">Prénom<input name="firstName" [(ngModel)]="form.firstName"></label><label class="field">Nom<input name="lastName" [(ngModel)]="form.lastName"></label><label class="field">Email<input name="email" [(ngModel)]="form.email"></label><label class="field">Téléphone<input name="phoneNumber" [(ngModel)]="form.phoneNumber"></label>
<label class="field">Pays<select name="countryId" [(ngModel)]="form.countryId"><option [ngValue]="null" disabled>Choisir un pays</option>@for(c of countries;track c.id){<option [ngValue]="c.id">{{c.name}}</option>}</select></label>
<label class="field">Mot de passe<input name="password" type="password" [(ngModel)]="form.password"></label><button class="btn primary">Créer</button><a routerLink="/auth/login" class="btn">Retour</a></form></div>`, styles:[`.reg{width:min(760px,100%)}`] })
export class RegisterComponent implements OnInit {
  done=false;
  countries: CountryResponse[] = [];
  form: ClientRegisterRequest = { firstName:'Client', lastName:'Demo', email:'new.client@okane.ma', phoneNumber:'+212600000001', password:'Password@123', countryId:1 };
  constructor(private auth: AuthService, private data: DataService) {}
  ngOnInit(): void { this.data.countries().subscribe(c => { this.countries = c.filter(x => x.active); }); }
  submit(){ this.auth.registerClient(this.form).subscribe(()=>this.done=true); }
}
