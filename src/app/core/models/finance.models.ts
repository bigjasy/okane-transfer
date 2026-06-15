import { CashMovementType, CashRegisterStatus } from './enums';
export interface CashRegisterOpenRequest { agentId: number; agencyId: number; currencyCode: string; openingBalance: number; }
export interface CashMovementRequest { type: CashMovementType; amount: number; currencyCode: string; reason: string; transferId?: number; }
export interface CashClosingRequest { countedAmount: number; comment: string; }
export interface CashRegisterResponse { id: number; agencyCode: string; agencyName?: string; agentName: string; currencyCode: string; openingBalance: number; currentBalance: number; status: CashRegisterStatus; openedAt?: string; closedAt?: string; }
export interface CashMovementResponse { id: number; type: CashMovementType; amount: number; currencyCode: string; transferReference?: string; reason: string; createdBy?: string; createdByName?: string; cashRegisterId?: number; createdAt: string; }
export interface CommissionResponse { id: number; transferReference: string; agencyPart: number; centralPart: number; currency: string; agencyName?: string; createdAt?: string; }
