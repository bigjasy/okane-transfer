import { Injectable } from '@angular/core';
import { delay, Observable, of, throwError } from 'rxjs';
import { LoginRequest, LoginResponse, JwtResponse, ClientRegisterRequest } from '../models/auth.models';
import { UserSummaryResponse, UserProfileResponse, UserCreateRequest, UserStatusUpdateRequest } from '../models/user.models';
import { AgencyResponse, CorridorResponse, FeeGridResponse, FeeSimulationRequest, FeeSimulationResponse } from '../models/agency.models';
import { CountryResponse, CurrencyResponse, ExchangeRateResponse, ConversionRequest, ConversionResponse } from '../models/referential.models';
import { AmlAlertResponse, KycDocumentResponse, KycDocumentUploadRequest, KycReviewRequest } from '../models/compliance.models';
import { TransferCreateRequest, TransferResponse, PayoutConfirmRequest, PayoutReceiptResponse, PayoutResponse, PayoutSearchRequest, PayoutSearchResponse, PayoutValidateRequest, PayoutValidateResponse, BeneficiaryRequest, BeneficiaryResponse, TransferTrackingResponse } from '../models/transfer.models';
import { CashClosingRequest, CashMovementRequest, CashMovementResponse, CashRegisterOpenRequest, CashRegisterResponse } from '../models/finance.models';
import { AuditLogResponse, ChatbotRequest, ChatbotResponse, MobileMoneyRequest, MobileMoneyResponse, NotificationResponse } from '../models/notification.models';
import { Role, MobileMoneyOperator } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class MockApiService {
  private users: UserSummaryResponse[] = [
    { id: 1, fullName: 'Admin Okane', email: 'admin@okane.ma', role: 'ROLE_ADMIN', status: 'ACTIVE', agencyName: 'Siège' },
    { id: 2, fullName: 'Manager Marrakech', email: 'manager@okane.ma', role: 'ROLE_MANAGER', status: 'ACTIVE', agencyName: 'Marrakech Centre' },
    { id: 3, fullName: 'Agent Guichet', email: 'agent@okane.ma', role: 'ROLE_AGENT', status: 'ACTIVE', agencyName: 'Marrakech Centre' },
    { id: 4, fullName: 'Client Demo', email: 'client@okane.ma', role: 'ROLE_CLIENT', status: 'ACTIVE', agencyName: 'Client' }
  ];
  currencies: CurrencyResponse[] = [
    { id: 1, code: 'MAD', name: 'Dirham marocain', symbol: 'DH', scale: 2, active: true },
    { id: 2, code: 'EUR', name: 'Euro', symbol: '€', scale: 2, active: true },
    { id: 3, code: 'USD', name: 'Dollar américain', symbol: '$', scale: 2, active: true }
  ];
  countries: CountryResponse[] = [
    { id: 1, isoCode: 'MA', name: 'Maroc', phonePrefix: '+212', currency: this.currencies[0], active: true },
    { id: 2, isoCode: 'FR', name: 'France', phonePrefix: '+33', currency: this.currencies[1], active: true },
    { id: 3, isoCode: 'US', name: 'États-Unis', phonePrefix: '+1', currency: this.currencies[2], active: true }
  ];
  agencies: AgencyResponse[] = [
    { id: 1, code: 'MA-001', name: 'Marrakech Centre', city: 'Marrakech', country: 'Maroc', status: 'ACTIVE', dailyLimit: 500000 },
    { id: 2, code: 'FR-001', name: 'Paris Nord', city: 'Paris', country: 'France', status: 'ACTIVE', dailyLimit: 400000 }
  ];
  corridors: CorridorResponse[] = [
    { id: 1, sourceCountry: this.countries[0], destinationCountry: this.countries[1], active: true, dailyLimit: 300000, monthlyLimit: 2000000 },
    { id: 2, sourceCountry: this.countries[1], destinationCountry: this.countries[0], active: true, dailyLimit: 250000, monthlyLimit: 1800000 }
  ];
  transfers: TransferResponse[] = [
    { id: 1, reference: 'OKN-2026-0001', status: 'AVAILABLE', senderName: 'Client Demo', beneficiaryName: 'Yassine El Amrani', sentAmount: 1200, feeAmount: 38, receivedAmount: 110, sourceCurrency: 'MAD', targetCurrency: 'EUR', exchangeRateApplied: 0.092, createdAt: new Date().toISOString(), expiresAt: new Date(Date.now()+7*86400000).toISOString() },
    { id: 2, reference: 'OKN-2026-0002', status: 'PAID', senderName: 'Client Demo', beneficiaryName: 'Sara Benali', sentAmount: 800, feeAmount: 27, receivedAmount: 73.5, sourceCurrency: 'MAD', targetCurrency: 'EUR', exchangeRateApplied: 0.092, createdAt: new Date(Date.now()-86400000).toISOString(), expiresAt: new Date(Date.now()+6*86400000).toISOString() }
  ];
  beneficiaries: BeneficiaryResponse[] = [
    { id: 1, fullName: 'Yassine El Amrani', phoneNumber: '+33612345678', country: 'France', identityType: 'PASSPORT', identityNumber: 'P123456' }
  ];
  cash: CashRegisterResponse = { id: 1, agencyCode: 'MA-001', agencyName: 'Marrakech Centre', agentName: 'Agent Guichet', currencyCode: 'MAD', openingBalance: 15000, currentBalance: 16200, status: 'OPEN', openedAt: new Date(Date.now() - 4 * 3600000).toISOString() };
  movements: CashMovementResponse[] = [
    { id: 1, type: 'TRANSFER_SEND', amount: 1200, currencyCode: 'MAD', transferReference: 'OKN-2026-0001', reason: 'Transfert OKN-2026-0001', createdBy: 'Agent Guichet', createdAt: new Date(Date.now() - 2 * 3600000).toISOString() }
  ];
  private payoutProfiles: Record<string, { withdrawalCode: string; beneficiaryPhoneNumber: string; identityType: 'CIN' | 'PASSPORT' | 'RESIDENCE_CARD'; identityNumber: string; agentName: string; agencyName: string; paidAt?: string }> = {
    'OKN-2026-0001': { withdrawalCode: '123456', beneficiaryPhoneNumber: '+33612345678', identityType: 'PASSPORT', identityNumber: 'P123456', agentName: 'Agent Guichet', agencyName: 'Marrakech Centre' },
    'OKN-2026-0002': { withdrawalCode: '654321', beneficiaryPhoneNumber: '+33698765432', identityType: 'CIN', identityNumber: 'CIN987654', agentName: 'Agent Guichet', agencyName: 'Marrakech Centre', paidAt: new Date(Date.now() - 86400000).toISOString() }
  };
  private kycDocumentsData: KycDocumentResponse[] = [
    { id: 1, userId: 4, userName: 'Client Demo', userEmail: 'client@okane.ma', documentType: 'CIN_FRONT', status: 'PENDING', uploadedAt: new Date(Date.now() - 3 * 3600000).toISOString() },
    { id: 2, userId: 4, userName: 'Client Demo', userEmail: 'client@okane.ma', documentType: 'PROOF_OF_ADDRESS', status: 'PENDING', uploadedAt: new Date(Date.now() - 2 * 3600000).toISOString() }
  ];
  private auditLogsData: AuditLogResponse[] = [
    {
      id: 1,
      actorUserId: 1,
      actorEmail: 'admin@okane.ma',
      action: 'LOGIN',
      entityType: 'User',
      entityId: '1',
      ipAddress: '127.0.0.1',
      userAgent: 'Mock browser',
      detailsJson: '{"result":"SUCCESS"}',
      createdAt: new Date(Date.now() - 30 * 60000).toISOString()
    },
    {
      id: 2,
      actorUserId: 1,
      actorEmail: 'admin@okane.ma',
      action: 'USER_STATUS_CHANGED',
      entityType: 'User',
      entityId: '3',
      ipAddress: '127.0.0.1',
      userAgent: 'Mock browser',
      detailsJson: '{"status":"ACTIVE"}',
      createdAt: new Date(Date.now() - 90 * 60000).toISOString()
    }
  ];
  private mobileMoneyTransfers: Map<number, MobileMoneyResponse> = new Map();

  login(req: LoginRequest): Observable<LoginResponse> {
    const map: Record<string, Role> = { 'admin@okane.ma': 'ROLE_ADMIN', 'manager@okane.ma': 'ROLE_MANAGER', 'agent@okane.ma': 'ROLE_AGENT', 'client@okane.ma': 'ROLE_CLIENT' };
    const role = map[req.email] ?? 'ROLE_CLIENT';
    const user = this.users.find(u => u.email === req.email) ?? this.users.find(u => u.role === role)!;
    const tokens: JwtResponse = { accessToken: `mock-access-${role}`, refreshToken: `mock-refresh-${role}`, tokenType: 'Bearer', expiresIn: 3600 };
    return of({ twoFactorRequired: false, tokens, user }).pipe(delay(250));
  }
  registerClient(req: ClientRegisterRequest): Observable<UserSummaryResponse> { const user: UserSummaryResponse = { id: Date.now(), email: req.email, fullName: `${req.firstName} ${req.lastName}`, role: 'ROLE_CLIENT', status: 'ACTIVE' }; this.users.push(user); return of(user).pipe(delay(250)); }
  getCurrentUser(): Observable<UserProfileResponse> { const user: UserProfileResponse = { id: 1, firstName: 'Admin', lastName: 'Okane', email: 'admin@okane.ma', phoneNumber: '+212600000000', role: 'ROLE_ADMIN', preferredLanguage: 'FR' }; return of(user).pipe(delay(150)); }
  dashboard(role: string): Observable<any> { return of({ totalVolume: 1245000, transferCount: 342, totalFees: 24550, totalCommissions: 13300, charts: { months: [90, 120, 170, 220, 300, 342], status: [120, 60, 30, 132] }, role }).pipe(delay(250)); }
  getUsers(): Observable<UserSummaryResponse[]> { return of([...this.users]).pipe(delay(200)); }
  createUser(body: UserCreateRequest): Observable<UserSummaryResponse> { const u: UserSummaryResponse = { id: Date.now(), email: body.email, fullName: `${body.firstName} ${body.lastName}`, role: body.role, status: 'ACTIVE', agencyName: body.agencyId ? 'Marrakech Centre' : '' }; this.users.push(u); return of(u).pipe(delay(200)); }
  updateUserStatus(id: number, body: UserStatusUpdateRequest): Observable<UserSummaryResponse> { const u = this.users.find(x => x.id === id)!; u.status = body.status; return of(u).pipe(delay(150)); }
  getCountries(): Observable<CountryResponse[]> { return of(this.countries).pipe(delay(200)); }
  getCurrencies(): Observable<CurrencyResponse[]> { return of(this.currencies).pipe(delay(200)); }
  getExchangeRates(): Observable<ExchangeRateResponse[]> { const rates: ExchangeRateResponse[] = [{ id: 1, sourceCurrency: 'MAD', targetCurrency: 'EUR', rate: 0.092, source: 'MANUAL', validFrom: new Date().toISOString(), active: true }]; return of(rates).pipe(delay(200)); }
  convertCurrency(req: ConversionRequest): Observable<ConversionResponse> { const rate = req.targetCurrency === 'EUR' ? 0.092 : 10.8; return of({ sourceAmount: req.amount, convertedAmount: +(req.amount * rate).toFixed(2), rate, sourceCurrency: req.sourceCurrency, targetCurrency: req.targetCurrency }).pipe(delay(150)); }
  getAgencies(): Observable<AgencyResponse[]> { return of(this.agencies).pipe(delay(200)); }
  getCorridors(): Observable<CorridorResponse[]> { return of(this.corridors).pipe(delay(200)); }
  getFeeGrids(): Observable<FeeGridResponse[]> { return of([{ id: 1, corridorId: this.corridors[0].id, corridor: this.corridors[0], sourceCurrency: this.currencies[0], targetCurrency: this.currencies[1], minAmount: 100, maxAmount: 10000, fixedFee: 20, percentageFee: 1.5, agencyCommissionRate: .5, centralCommissionRate: 1, validFrom: '2026-01-01', validTo: '2026-12-31', active: true }]).pipe(delay(200)); }
  simulateFees(req: FeeSimulationRequest): Observable<FeeSimulationResponse> { const fee = +(20 + req.amount * 0.015).toFixed(2); const rate = req.targetCurrency === 'EUR' ? .092 : 1; const received = +(req.amount * rate).toFixed(2); return of({ amount: req.amount, feeAmount: fee, totalToPay: +(req.amount+fee).toFixed(2), exchangeRate: rate, receivedAmount: received, agencyCommission: +(fee*.35).toFixed(2), centralCommission: +(fee*.65).toFixed(2) }).pipe(delay(200)); }
  getTransfers(): Observable<TransferResponse[]> { return of([...this.transfers]).pipe(delay(200)); }
  getTransferByReference(ref: string): Observable<TransferResponse> { return of(this.transfers.find(t => t.reference === ref) ?? this.transfers[0]).pipe(delay(150)); }
  trackTransfer(ref: string): Observable<TransferTrackingResponse> { const t = this.transfers.find(x => x.reference === ref) ?? this.transfers[0]; return of({ reference: t.reference, status: t.status, sourceCountry: 'Maroc', destinationCountry: 'France', receivedAmount: t.receivedAmount, createdAt: t.createdAt, paidAt: t.status === 'PAID' ? new Date().toISOString() : undefined }).pipe(delay(150)); }
  createTransfer(req: TransferCreateRequest): Observable<TransferResponse> { const t: TransferResponse = { id: Date.now(), reference: `OKN-${new Date().getFullYear()}-${Math.floor(Math.random()*9000+1000)}`, status: 'AVAILABLE', senderName: `Client #${req.senderClientId}`, beneficiaryName: `${req.beneficiary.firstName} ${req.beneficiary.lastName}`, sentAmount: req.amount, feeAmount: +(20+req.amount*.015).toFixed(2), receivedAmount: +(req.amount*.092).toFixed(2), sourceCurrency: req.sourceCurrency, targetCurrency: req.targetCurrency, exchangeRateApplied: .092, createdAt: new Date().toISOString(), expiresAt: new Date(Date.now()+7*86400000).toISOString() }; this.transfers.unshift(t); return of(t).pipe(delay(250)); }
  searchPayouts(req: PayoutSearchRequest): Observable<PayoutSearchResponse> {
    const transfer = this.transfers.find(item => {
      const profile = this.payoutProfiles[item.reference];
      if (!profile) return false;

      const matchesCode = !!req.withdrawalCode && profile.withdrawalCode === req.withdrawalCode;
      const matchesPhone = !!req.beneficiaryPhoneNumber && profile.beneficiaryPhoneNumber === req.beneficiaryPhoneNumber;
      return matchesCode || matchesPhone;
    });

    if (!transfer) return throwError(() => ({ status: 404, message: 'Transfer not found' }));

    const profile = this.payoutProfiles[transfer.reference];
    return of({
      ...transfer,
      beneficiaryPhoneNumber: profile?.beneficiaryPhoneNumber,
      withdrawalCode: profile?.withdrawalCode,
      agentName: profile?.agentName,
      agencyName: profile?.agencyName
    }).pipe(delay(200));
  }
  validatePayout(req: PayoutValidateRequest): Observable<PayoutValidateResponse> {
    const profile = this.payoutProfiles[req.transferReference];
    const valid = !!profile
      && profile.withdrawalCode === req.withdrawalCode
      && profile.identityType === req.identityType
      && profile.identityNumber === req.identityNumber;

    return of({ valid, requiresOtp: valid, message: valid ? 'VALID' : 'INVALID_IDENTITY' }).pipe(delay(200));
  }
  confirmPayout(req: PayoutConfirmRequest): Observable<PayoutResponse> {
    const transfer = this.transfers.find(item => item.reference === req.transferReference) ?? this.transfers[0];
    const profile = this.payoutProfiles[transfer.reference];
    const paidAt = new Date().toISOString();

    transfer.status = 'PAID';
    if (profile) profile.paidAt = paidAt;

    const payout: PayoutResponse = {
      transferReference: transfer.reference,
      status: transfer.status,
      paidAmount: transfer.receivedAmount,
      currency: transfer.targetCurrency,
      paidAt,
      beneficiaryName: transfer.beneficiaryName,
      agentName: profile?.agentName,
      agencyName: profile?.agencyName,
      maskedIdentityNumber: this.maskIdentity(req.identityNumber)
    };
    return of(payout).pipe(delay(250));
  }
  payoutReceipt(transferReference: string): Observable<PayoutReceiptResponse> {
    const transfer = this.transfers.find(item => item.reference === transferReference) ?? this.transfers[0];
    const profile = this.payoutProfiles[transfer.reference];
    const receipt: PayoutReceiptResponse = {
      transferReference: transfer.reference,
      beneficiaryName: transfer.beneficiaryName,
      paidAmount: transfer.receivedAmount,
      currency: transfer.targetCurrency,
      paidAt: profile?.paidAt ?? new Date().toISOString(),
      status: transfer.status,
      agentName: profile?.agentName,
      agencyName: profile?.agencyName,
      maskedIdentityNumber: this.maskIdentity(profile?.identityNumber ?? '')
    };
    return of(receipt).pipe(delay(150));
  }
  getCurrentCashRegister(): Observable<CashRegisterResponse> { return of(this.cash).pipe(delay(150)); }
  openCashRegister(req: CashRegisterOpenRequest): Observable<CashRegisterResponse> { this.cash = { id: Date.now(), agencyCode: `AG-${req.agencyId}`, agencyName: req.agencyId === 1 ? 'Marrakech Centre' : `Agence #${req.agencyId}`, agentName: `Agent #${req.agentId}`, currencyCode: req.currencyCode, openingBalance: req.openingBalance, currentBalance: req.openingBalance, status: 'OPEN', openedAt: new Date().toISOString() }; return of(this.cash).pipe(delay(200)); }
  addCashMovement(req: CashMovementRequest): Observable<CashMovementResponse> { const mv: CashMovementResponse = { id: Date.now(), type: req.type, amount: req.amount, currencyCode: req.currencyCode, transferReference: req.transferId ? `#${req.transferId}` : undefined, reason: req.reason, createdBy: this.cash.agentName, createdAt: new Date().toISOString() }; this.movements.unshift(mv); this.cash.currentBalance += req.type.includes('OUT') || req.type === 'TRANSFER_PAYOUT' ? -req.amount : req.amount; return of(mv).pipe(delay(200)); }
  closeCashRegister(req: CashClosingRequest): Observable<CashRegisterResponse> {
    const difference = +(req.countedAmount - this.cash.currentBalance).toFixed(2);
    if (difference !== 0) {
      this.movements.unshift({ id: Date.now(), type: 'CLOSING_DIFFERENCE', amount: difference, currencyCode: this.cash.currencyCode, reason: `Écart de clôture. ${req.comment}`, createdBy: this.cash.agentName, createdAt: new Date().toISOString() });
      this.cash.currentBalance = req.countedAmount;
    }
    this.cash.status = 'CLOSED';
    this.cash.closedAt = new Date().toISOString();
    return of(this.cash).pipe(delay(200));
  }
  getCashMovements(): Observable<CashMovementResponse[]> { return of(this.movements).pipe(delay(150)); }
  getBeneficiaries(): Observable<BeneficiaryResponse[]> { return of(this.beneficiaries).pipe(delay(150)); }
  createBeneficiary(b: BeneficiaryRequest): Observable<BeneficiaryResponse> {
    const out: BeneficiaryResponse = { id: Date.now(), fullName: `${b.firstName} ${b.lastName}`, phoneNumber: b.phoneNumber, country: this.countryName(b.countryId) };
    this.beneficiaries.push(out);
    return of(out).pipe(delay(150));
  }
  updateBeneficiary(id: number, b: BeneficiaryRequest): Observable<BeneficiaryResponse> {
    const out: BeneficiaryResponse = { id, fullName: `${b.firstName} ${b.lastName}`, phoneNumber: b.phoneNumber, country: this.countryName(b.countryId) };
    this.beneficiaries = this.beneficiaries.map(x => x.id === id ? out : x);
    return of(out).pipe(delay(150));
  }
  deleteBeneficiary(id: number): Observable<void> { this.beneficiaries = this.beneficiaries.filter(b => b.id !== id); return of(void 0).pipe(delay(150)); }
  kycDocuments(): Observable<KycDocumentResponse[]> { return of(this.kycDocumentsData.filter(document => document.status === 'PENDING').map(document => ({ ...document }))).pipe(delay(150)); }
  clientKycDocuments(): Observable<KycDocumentResponse[]> { return of(this.kycDocumentsData.filter(document => document.userId === 4).map(document => ({ ...document }))).pipe(delay(150)); }
  uploadClientKycDocument(req: KycDocumentUploadRequest): Observable<KycDocumentResponse> {
    const document: KycDocumentResponse = {
      id: Date.now(),
      userId: 4,
      userName: 'Client Demo',
      userEmail: 'client@okane.ma',
      documentType: req.documentType,
      status: 'PENDING',
      uploadedAt: new Date().toISOString()
    };
    this.kycDocumentsData.unshift(document);
    return of({ ...document }).pipe(delay(200));
  }
  reviewKycDocument(id: number, req: KycReviewRequest): Observable<KycDocumentResponse> {
    const document = this.kycDocumentsData.find(item => item.id === id);
    if (!document) return throwError(() => ({ status: 404, message: 'KYC document not found' }));
    document.status = req.status;
    document.rejectionReason = req.status === 'REJECTED' ? req.rejectionReason : undefined;
    return of({ ...document }).pipe(delay(150));
  }
  amlAlerts(): Observable<AmlAlertResponse[]> { const alerts: AmlAlertResponse[] = [{ id: 1, transferReference: 'OKN-2026-0001', type: 'THRESHOLD_EXCEEDED', riskLevel: 'HIGH', status: 'OPEN', description: 'Montant inhabituel sur corridor sensible', createdAt: new Date().toISOString() }]; return of(alerts).pipe(delay(150)); }
  notifications(): Observable<NotificationResponse[]> { const notifications: NotificationResponse[] = [{ id: 1, channel: 'EMAIL', title: 'Transfert disponible', message: 'Votre transfert est disponible au retrait.', status: 'SENT', createdAt: new Date().toISOString() }]; return of(notifications).pipe(delay(150)); }
  auditLogs(): Observable<AuditLogResponse[]> { return of(this.auditLogsData.map(log => ({ ...log }))).pipe(delay(150)); }
  auditLog(id: number): Observable<AuditLogResponse> {
    const log = this.auditLogsData.find(item => item.id === id);
    if (!log) return throwError(() => ({ status: 404, message: 'Audit log not found' }));
    return of({ ...log }).pipe(delay(150));
  }
  createMobileMoneyTransfer(req: MobileMoneyRequest): Observable<MobileMoneyResponse> {
    const mobileMoney: MobileMoneyResponse = {
      id: Date.now(),
      transferReference: req.transferReference,
      operator: req.operator,
      status: 'SENT_TO_OPERATOR',
      reconciliationStatus: 'NOT_RECONCILED',
      operatorTransactionReference: 'OP-' + Math.floor(Math.random() * 999999)
    };
    this.mobileMoneyTransfers.set(mobileMoney.id, mobileMoney);
    return of(mobileMoney).pipe(delay(250));
  }

  mobileMoneySimulateCallback(id: number): Observable<MobileMoneyResponse> {
    const transfer = this.mobileMoneyTransfers.get(id);
    if (!transfer) {
      const newTransfer: MobileMoneyResponse = {
        id,
        transferReference: '',
        operator: 'ORANGE_MONEY',
        status: 'CONFIRMED',
        reconciliationStatus: 'NOT_RECONCILED',
        operatorTransactionReference: 'OP-' + Math.floor(Math.random() * 999999)
      };
      this.mobileMoneyTransfers.set(id, newTransfer);
      return of(newTransfer).pipe(delay(250));
    }
    transfer.status = 'CONFIRMED';
    return of({ ...transfer }).pipe(delay(250));
  }

  mobileMoneySimulateReconciliation(id: number): Observable<MobileMoneyResponse> {
    const transfer = this.mobileMoneyTransfers.get(id);
    if (!transfer) {
      const newTransfer: MobileMoneyResponse = {
        id,
        transferReference: '',
        operator: 'ORANGE_MONEY',
        status: 'CONFIRMED',
        reconciliationStatus: 'RECONCILED',
        operatorTransactionReference: 'OP-' + Math.floor(Math.random() * 999999)
      };
      this.mobileMoneyTransfers.set(id, newTransfer);
      return of(newTransfer).pipe(delay(250));
    }
    transfer.reconciliationStatus = 'RECONCILED';
    return of({ ...transfer }).pipe(delay(250));
  }

  mobileMoneyList(): Observable<MobileMoneyResponse[]> {
    const items: MobileMoneyResponse[] = [
      {
        id: 1,
        transferReference: 'OKN-2026-0098',
        operator: 'ORANGE_MONEY' as MobileMoneyOperator,
        walletPhoneNumber: '+212700123456',
        status: 'CONFIRMED',
        reconciliationStatus: 'RECONCILED',
        operatorTransactionReference: 'OM-123456',
        createdAt: new Date(Date.now() - 3600000).toISOString()
      },
      {
        id: 2,
        transferReference: 'OKN-2026-0097',
        operator: 'WAVE' as MobileMoneyOperator,
        walletPhoneNumber: '+221785234567',
        status: 'CONFIRMED',
        reconciliationStatus: 'RECONCILED',
        operatorTransactionReference: 'WV-234567',
        createdAt: new Date(Date.now() - 7200000).toISOString()
      }
    ];
    return of(items).pipe(delay(150));
  }

  processChatbotMessage(req: ChatbotRequest): Observable<ChatbotResponse> { const msg = req.message.toLowerCase(); const intent = msg.includes('frais') || msg.includes('fees') ? 'FEES' : msg.includes('status') || msg.includes('référence') || msg.includes('reference') ? 'TRACKING' : msg.includes('agent') || msg.includes('support') ? 'ESCALATION' : 'FAQ'; const answer = intent === 'FEES' ? 'Vous pouvez simuler les frais depuis Agent > Simulation frais.' : intent === 'TRACKING' ? 'Entrez la référence dans Suivi transfert pour connaître le statut.' : intent === 'ESCALATION' ? 'Votre demande est escaladée vers un agent.' : 'OkaneTransfer permet d’envoyer, suivre et retirer des transferts.'; return of({ answer, intent, escalated: intent === 'ESCALATION' }).pipe(delay(300)); }

  private maskIdentity(value: string): string {
    if (value.length <= 4) return value ? '****' : '';
    return `${value.slice(0, 2)}****${value.slice(-2)}`;
  }

  private countryName(countryId: number): string {
    return this.countries.find(country => country.id === countryId)?.name ?? 'Pays inconnu';
  }
}
