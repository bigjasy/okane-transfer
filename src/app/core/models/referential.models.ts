import { RateSource } from './enums';
export interface CurrencyRequest { code: string; name: string; symbol: string; scale: number; active: boolean; }
export interface CurrencyResponse { id: number; code: string; name: string; symbol: string; scale: number; active: boolean; }
export interface CountryRequest { isoCode: string; name: string; phonePrefix: string; currencyId: number; active: boolean; }
export interface CountryResponse { id: number; isoCode: string; name: string; phonePrefix: string; currency: CurrencyResponse; active: boolean; }
export interface ExchangeRateRequest { sourceCurrencyId: number; targetCurrencyId: number; rate: number; source: RateSource; }
export interface ExchangeRateResponse { id: number; sourceCurrency: string; targetCurrency: string; rate: number; source: RateSource; validFrom: string; active: boolean; }
export interface ExchangeRateHistoryResponse { id: number; sourceCurrencyCode: string; targetCurrencyCode: string; oldRate: number; newRate: number; source: RateSource; changedByEmail: string; changedAt: string; }
export interface ConversionRequest { sourceCurrency: string; targetCurrency: string; amount: number; }
export interface ConversionResponse { sourceAmount: number; convertedAmount: number; rate: number; sourceCurrency: string; targetCurrency: string; }
