import { IdentityType, TransferChannel, TransferStatus } from './enums';
export interface BeneficiaryRequest { id?: number; firstName: string; lastName: string; phoneNumber: string; countryId: number; identityType: IdentityType; identityNumber: string; }
export interface BeneficiaryResponse { id: number; fullName: string; phoneNumber: string; country: string; identityType?: IdentityType; identityNumber?: string; }
export interface TransferCreateRequest { senderClientId: number; beneficiary: BeneficiaryRequest; sourceAgencyId: number; destinationAgencyId: number; corridorId: number; sourceCurrency: string; targetCurrency: string; amount: number; channel: TransferChannel; }
export interface TransferResponse { id: number; reference: string; status: TransferStatus; senderName: string; beneficiaryName: string; sentAmount: number; feeAmount: number; receivedAmount: number; sourceCurrency: string; targetCurrency: string; exchangeRateApplied: number; createdAt: string; expiresAt: string; }
export interface TransferConfirmRequest { otpCode: string; }
export interface TransferCancelRequest { reason: string; }
export interface TransferTrackingResponse { reference: string; status: TransferStatus; sourceCountry: string; destinationCountry: string; receivedAmount: number; createdAt: string; paidAt?: string; }
export interface PayoutSearchRequest { withdrawalCode: string; beneficiaryPhoneNumber: string; }
export interface PayoutConfirmRequest { transferReference: string; withdrawalCode: string; identityType: IdentityType; identityNumber: string; otpCode: string; }
export interface PayoutResponse { transferReference: string; status: TransferStatus; paidAmount: number; currency: string; paidAt: string; }
