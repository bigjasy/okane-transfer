import { Injectable, signal } from '@angular/core';
import { Language } from '../models/enums';
@Injectable({ providedIn: 'root' })
export class I18nService {
  readonly language = signal<Language>((localStorage.getItem('okane.lang') as Language) || 'FR');
  setLanguage(lang: Language): void { localStorage.setItem('okane.lang', lang); this.language.set(lang); }
}
