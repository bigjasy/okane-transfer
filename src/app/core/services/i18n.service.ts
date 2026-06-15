import { Injectable, signal, computed } from '@angular/core';
import { Language } from '../models/enums';
import enTranslations from '../../i18n/en.json';
import frTranslations from '../../i18n/fr.json';
import arTranslations from '../../i18n/ar.json';

@Injectable({ providedIn: 'root' })
export class I18nService {
  readonly language = signal<Language>((localStorage.getItem('okane.lang') as Language) || 'FR');
  
  private translations = {
    EN: enTranslations,
    FR: frTranslations,
    AR: arTranslations
  };

  readonly currentTranslations = computed(() => {
    const lang = this.language();
    return this.translations[lang as keyof typeof this.translations] || this.translations.EN;
  });

  setLanguage(lang: Language): void {
    localStorage.setItem('okane.lang', lang);
    this.language.set(lang);
  }

  get(key: string): string {
    const keys = key.split('.');
    let value: any = this.currentTranslations();
    
    for (const k of keys) {
      value = value?.[k];
    }
    
    return typeof value === 'string' ? value : key;
  }

  translate(key: string, variables?: Record<string, string>): string {
    let text = this.get(key);
    
    if (variables) {
      Object.entries(variables).forEach(([k, v]) => {
        text = text.replace(`{{${k}}}`, v);
      });
    }
    
    return text;
  }
}
