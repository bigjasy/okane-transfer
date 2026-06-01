import { AmlAlertStatus, AmlAlertType, KycDocumentType, KycStatus, RiskLevel } from './enums';
export interface KycDocumentResponse { id: number; documentType: KycDocumentType; status: KycStatus; uploadedAt: string; rejectionReason?: string; }
export interface KycReviewRequest { status: KycStatus; rejectionReason?: string; }
export interface AmlAlertResponse { id: number; transferReference: string; type: AmlAlertType; riskLevel: RiskLevel; status: AmlAlertStatus; description: string; createdAt: string; }
export interface AmlReviewRequest { status: AmlAlertStatus; comment?: string; }
export interface AmlCheckTransferRequest { transferReference: string; }
export interface AmlCheckTransferResponse { blocked: boolean; alerts: AmlAlertResponse[]; }
export interface WatchlistEntryRequest { firstName: string; lastName: string; countryId: number; source: string; active: boolean; }
export interface WatchlistEntryResponse { id: number; firstName: string; lastName: string; countryName: string; source: string; active: boolean; createdAt: string; }
