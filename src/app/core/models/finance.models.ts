import { CashMovementType, CashRegisterStatus } from './enums';
export interface CashRegisterOpenRequest { agentId: number; agencyId: number; currencyCode: string; openingBalance: number; }
export interface CashMovementRequest { type: CashMovementType; amount: number; currencyCode: string; reason: string; transferId?: number; }
export interface CashClosingRequest { countedAmount: number; comment: string; }
export interface CashRegisterResponse { id: number; agencyCode: string; agentName: string; currencyCode: string; openingBalance: number; currentBalance: number; status: CashRegisterStatus; }
export interface CashMovementResponse { id: number; type: CashMovementType; amount: number; currencyCode: string; reason: string; createdAt: string; }
export interface CommissionResponse { id: number; transferReference: string; agencyPart: number; centralPart: number; currency: string; }
