import { AgencyStatus } from './enums';
import { CountryResponse, CurrencyResponse } from './referential.models';
export interface AgencyRequest { code: string; name: string; address: string; city: string; countryId: number; dailyLimit: number; }
export interface AgencyResponse { id: number; code: string; name: string; city: string; address?: string; country: string; status: AgencyStatus; dailyLimit: number; }
export interface AgencyStaffResponse { agencyId: number; agencyCode: string; agencyName: string; agents: import('./user.models').UserSummaryResponse[]; managers: import('./user.models').UserSummaryResponse[]; }
export interface CorridorRequest { sourceCountryId: number; destinationCountryId: number; dailyLimit: number; monthlyLimit: number; active: boolean; }
export interface CorridorResponse { id: number; sourceCountry: CountryResponse; destinationCountry: CountryResponse; active: boolean; dailyLimit: number; monthlyLimit: number; }
export interface FeeGridRequest {
  corridorId: number;
  minAmount: number;
  maxAmount: number;
  fixedFee: number;
  percentageFee: number;
  agencyCommissionRate: number;
  centralCommissionRate: number;
  validFrom?: string;
  validTo?: string;
  active: boolean;
}
export interface FeeGridResponse {
  id: number;
  corridorId?: number;
  corridor?: CorridorResponse;
  sourceCurrency?: CurrencyResponse;
  targetCurrency?: CurrencyResponse;
  minAmount: number;
  maxAmount: number;
  fixedFee: number;
  percentageFee: number;
  agencyCommissionRate: number;
  centralCommissionRate: number;
  validFrom?: string;
  validTo?: string;
  active: boolean;
}
export interface FeeSimulationRequest { corridorId: number; sourceCurrency: string; targetCurrency: string; amount: number; }
export interface FeeSimulationResponse { amount: number; feeAmount: number; totalToPay: number; exchangeRate: number; receivedAmount: number; agencyCommission: number; centralCommission: number; }
