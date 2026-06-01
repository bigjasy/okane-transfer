import { MobileMoneyOperator, MobileMoneyStatus, NotificationChannel, NotificationStatus, ReconciliationStatus } from './enums';
export interface NotificationResponse { id: number; channel: NotificationChannel; title: string; message: string; status: NotificationStatus; createdAt: string; }
export interface NotificationPreferencesResponse { email: boolean; sms: boolean; push: boolean; }
export interface MobileMoneyRequest { transferReference: string; operator: MobileMoneyOperator; walletPhoneNumber: string; }
export interface MobileMoneyResponse { id: number; transferReference: string; operator: MobileMoneyOperator; status: MobileMoneyStatus; reconciliationStatus: ReconciliationStatus; operatorTransactionReference: string; }
export interface ChatbotRequest { message: string; language: string; }
export interface ChatbotResponse { answer: string; escalated: boolean; intent: string; }
export interface AuditLogResponse { id?: number; actorEmail: string; action: string; entityType: string; entityId: string; ipAddress: string; createdAt: string; }
