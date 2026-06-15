import { AmlAlertStatus, AmlAlertType, KycDocumentType, KycStatus, RiskLevel } from './enums';
export interface KycDocumentResponse { id: number; userId?: number; userName?: string; userEmail?: string; clientName?: string; documentType: KycDocumentType; status: KycStatus; uploadedAt: string; rejectionReason?: string; }
export interface KycDocumentUploadRequest { file: File; documentType: KycDocumentType; documentNumber: string; }
export type KycReviewStatus = Extract<KycStatus, 'APPROVED' | 'REJECTED'>;
export interface KycReviewRequest { status: KycReviewStatus; rejectionReason?: string; }
export interface AmlAlertResponse { id: number; transferReference: string; type: AmlAlertType; riskLevel: RiskLevel; status: AmlAlertStatus; description: string; createdAt: string; }
export interface AmlReviewRequest { status: AmlAlertStatus; comment?: string; }
export interface AmlCheckTransferRequest { transferReference: string; }
export interface AmlCheckTransferResponse { blocked: boolean; alerts: AmlAlertResponse[]; }
export interface WatchlistEntryRequest { firstName: string; lastName: string; countryId: number; source: string; active: boolean; }
export interface WatchlistEntryResponse { id: number; firstName: string; lastName: string; countryName: string; source: string; active: boolean; createdAt: string; }

export interface ComplianceSummaryResponse {
  pendingKycDocuments: number;
  openAmlAlerts: number;
  criticalAmlAlerts: number;
  activeWatchlistEntries: number;
  blockedTransfers: number;
}

export interface ExchangeRateSyncResponse {
  provider: string;
  source: string;
  syncedAt: string;
  updatedCount: number;
  rates: import('./referential.models').ExchangeRateResponse[];
}
