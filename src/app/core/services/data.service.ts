import { Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiService } from './api.service';
import { MockApiService } from './mock-api.service';
import { ApiResponse, BackendPage, PageResponse } from '../models/common.models';
import { UserCreateRequest, UserProfileResponse, UserStatusUpdateRequest, UserSummaryResponse, UserUpdateRequest } from '../models/user.models';
import { AgencyResponse, CorridorResponse, FeeGridResponse, FeeSimulationRequest, FeeSimulationResponse } from '../models/agency.models';
import { ConversionRequest, ConversionResponse, CountryResponse, CurrencyResponse, ExchangeRateResponse } from '../models/referential.models';
import { AmlAlertResponse, KycDocumentResponse } from '../models/compliance.models';
import { TransferCreateRequest, TransferResponse, PayoutConfirmRequest, PayoutResponse, BeneficiaryRequest, BeneficiaryResponse, TransferTrackingResponse } from '../models/transfer.models';
import { CashClosingRequest, CashMovementRequest, CashMovementResponse, CashRegisterOpenRequest, CashRegisterResponse } from '../models/finance.models';
import { AuditLogResponse, ChatbotRequest, ChatbotResponse, MobileMoneyRequest, MobileMoneyResponse, NotificationResponse } from '../models/notification.models';
import { UserStatus } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class DataService {
  constructor(private readonly api: ApiService, private readonly mock: MockApiService) {}
  private useMock(): boolean { return environment.useMockApi; }
  private fallback<T>(obs: Observable<T>, fb: Observable<T>): Observable<T> { return environment.allowMockFallback ? obs.pipe(catchError(err => { console.warn('Backend indisponible, fallback mock:', err); return fb; })) : obs; }
  private pageToList<T>(page: BackendPage<T> | PageResponse<T> | T[]): T[] { return Array.isArray(page) ? page : page.content; }
  private unwrap<T>(r: T | ApiResponse<T>): T { return r && typeof r === 'object' && 'data' in r && (r as ApiResponse<T>).data ? (r as ApiResponse<T>).data as T : r as T; }

  dashboard(role: string): Observable<any> { return this.useMock() ? this.mock.dashboard(role) : this.fallback(this.api.get<any>(`/dashboards/${role}`).pipe(catchError(()=>this.mock.dashboard(role))), this.mock.dashboard(role)); }
  users(): Observable<UserSummaryResponse[]> { const real = this.api.get<BackendPage<UserSummaryResponse> | UserSummaryResponse[]>('/users').pipe((source) => new Observable<UserSummaryResponse[]>(sub => source.subscribe({ next: v => { sub.next(this.pageToList(v)); sub.complete(); }, error: e => sub.error(e) }))); return this.useMock() ? this.mock.getUsers() : this.fallback(real, this.mock.getUsers()); }
  createUser(body: UserCreateRequest): Observable<UserSummaryResponse> { return this.useMock() ? this.mock.createUser(body) : this.fallback(this.api.post<UserSummaryResponse>('/users', body), this.mock.createUser(body)); }
  updateUserStatus(id: number, status: UserStatus): Observable<UserSummaryResponse> { return this.useMock() ? this.mock.updateUserStatus(id, { status }) : this.fallback(this.api.patch<UserSummaryResponse>(`/users/${id}/status`, { status }), this.mock.updateUserStatus(id, { status } as UserStatusUpdateRequest)); }
  countries(): Observable<CountryResponse[]> { return this.useMock() ? this.mock.getCountries() : this.fallback(this.api.get<CountryResponse[]>('/countries'), this.mock.getCountries()); }
  currencies(): Observable<CurrencyResponse[]> { return this.useMock() ? this.mock.getCurrencies() : this.fallback(this.api.get<CurrencyResponse[]>('/currencies'), this.mock.getCurrencies()); }
  rates(): Observable<ExchangeRateResponse[]> { return this.useMock() ? this.mock.getExchangeRates() : this.fallback(this.api.get<ExchangeRateResponse[]>('/exchange-rates'), this.mock.getExchangeRates()); }
  convert(body: ConversionRequest): Observable<ConversionResponse> { return this.useMock() ? this.mock.convertCurrency(body) : this.fallback(this.api.post<ConversionResponse>('/exchange-rates/convert', body), this.mock.convertCurrency(body)); }
  agencies(): Observable<AgencyResponse[]> { const real = this.api.get<BackendPage<AgencyResponse> | AgencyResponse[]>('/agencies').pipe((source) => new Observable<AgencyResponse[]>(sub => source.subscribe({ next: v => { sub.next(this.pageToList(v)); sub.complete(); }, error: e => sub.error(e) }))); return this.useMock() ? this.mock.getAgencies() : this.fallback(real, this.mock.getAgencies()); }
  corridors(): Observable<CorridorResponse[]> { return this.useMock() ? this.mock.getCorridors() : this.fallback(this.api.get<CorridorResponse[]>('/corridors'), this.mock.getCorridors()); }
  feeGrids(): Observable<FeeGridResponse[]> { return this.mock.getFeeGrids(); }
  simulateFees(body: FeeSimulationRequest): Observable<FeeSimulationResponse> { return this.mock.simulateFees(body); }
  transfers(): Observable<TransferResponse[]> { return this.useMock() ? this.mock.getTransfers() : this.fallback(this.api.get<TransferResponse[]>('/transfers'), this.mock.getTransfers()); }
  transfer(ref: string): Observable<TransferResponse> { return this.useMock() ? this.mock.getTransferByReference(ref) : this.fallback(this.api.get<TransferResponse>(`/transfers/${ref}`), this.mock.getTransferByReference(ref)); }
  trackTransfer(ref: string): Observable<TransferTrackingResponse> { return this.useMock() ? this.mock.trackTransfer(ref) : this.fallback(this.api.get<TransferTrackingResponse>(`/transfers/track/${ref}`), this.mock.trackTransfer(ref)); }
  createTransfer(body: TransferCreateRequest): Observable<TransferResponse> { return this.useMock() ? this.mock.createTransfer(body) : this.fallback(this.api.post<TransferResponse>('/transfers', body), this.mock.createTransfer(body)); }
  confirmPayout(body: PayoutConfirmRequest): Observable<PayoutResponse> { return this.useMock() ? this.mock.confirmPayout(body) : this.fallback(this.api.post<PayoutResponse>('/payouts/confirm', body), this.mock.confirmPayout(body)); }
  searchPayout(): Observable<TransferResponse> { return this.mock.searchPayouts(); }
  validatePayout(): Observable<{ valid: boolean }> { return this.mock.validatePayout(); }
  currentCash(): Observable<CashRegisterResponse> { return this.useMock() ? this.mock.getCurrentCashRegister() : this.fallback(this.api.get<CashRegisterResponse>('/cash-registers/current', { currencyCode: 'MAD' }), this.mock.getCurrentCashRegister()); }
  openCash(body: CashRegisterOpenRequest): Observable<CashRegisterResponse> { return this.useMock() ? this.mock.openCashRegister(body) : this.fallback(this.api.post<CashRegisterResponse>('/cash-registers/open', body), this.mock.openCashRegister(body)); }
  addMovement(id: number, body: CashMovementRequest): Observable<CashMovementResponse> { return this.mock.addCashMovement(body); }
  closeCash(id: number, body: CashClosingRequest): Observable<CashRegisterResponse> { return this.useMock() ? this.mock.closeCashRegister(body) : this.fallback(this.api.post<CashRegisterResponse>(`/cash-registers/${id}/close`, body), this.mock.closeCashRegister(body)); }
  movements(id = 1): Observable<CashMovementResponse[]> { return this.mock.getCashMovements(); }
  currentUserProfile(): Observable<UserProfileResponse> {
    console.info('[Profile] loading /auth/me');
    return this.useMock() ? this.mock.getCurrentUser() : this.fallback(
      this.api.get<UserProfileResponse | ApiResponse<UserProfileResponse>>('/auth/me').pipe(map(r => this.unwrap(r))),
      this.mock.getCurrentUser()
    );
  }
  updateUserProfile(id: number, body: UserUpdateRequest): Observable<UserProfileResponse> {
    if (this.useMock()) return this.mock.getCurrentUser();
    return this.api.put<UserProfileResponse | ApiResponse<UserProfileResponse>>(`/users/${id}`, body).pipe(map(r => this.unwrap(r)));
  }
  beneficiaries(): Observable<BeneficiaryResponse[]> {
    return this.useMock() ? this.mock.getBeneficiaries() : this.fallback(
      this.api.get<BeneficiaryResponse[] | ApiResponse<BeneficiaryResponse[]>>('/beneficiaries').pipe(map(r => this.unwrap(r))).pipe(map(r => Array.isArray(r) ? r : [])),
      this.mock.getBeneficiaries()
    );
  }
  createBeneficiary(body: BeneficiaryRequest): Observable<BeneficiaryResponse> {
    return this.useMock() ? this.mock.createBeneficiary(body) : this.fallback(
      this.api.post<BeneficiaryResponse | ApiResponse<BeneficiaryResponse>>('/beneficiaries', body).pipe(map(r => this.unwrap(r))),
      this.mock.createBeneficiary(body)
    );
  }
  updateBeneficiary(id: number, body: BeneficiaryRequest): Observable<BeneficiaryResponse> {
    return this.useMock() ? this.mock.updateBeneficiary(id, body) : this.fallback(
      this.api.put<BeneficiaryResponse | ApiResponse<BeneficiaryResponse>>(`/beneficiaries/${id}`, body).pipe(map(r => this.unwrap(r))),
      this.mock.updateBeneficiary(id, body)
    );
  }
  deleteBeneficiary(id: number): Observable<void> {
    return this.useMock() ? this.mock.deleteBeneficiary(id) : this.fallback(
      this.api.delete<void>(`/beneficiaries/${id}`),
      this.mock.deleteBeneficiary(id)
    );
  }
  kycDocuments(): Observable<KycDocumentResponse[]> { return this.mock.kycDocuments(); }
  amlAlerts(): Observable<AmlAlertResponse[]> { return this.useMock() ? this.mock.amlAlerts() : this.fallback(this.api.get<PageResponse<AmlAlertResponse>>('/aml/alerts').pipe((source) => new Observable<AmlAlertResponse[]>(sub => source.subscribe({ next: v => { sub.next(v.content); sub.complete(); }, error: e => sub.error(e) }))), this.mock.amlAlerts()); }
  notifications(): Observable<NotificationResponse[]> { return this.mock.notifications(); }
  auditLogs(): Observable<AuditLogResponse[]> { return this.mock.auditLogs(); }
  mobileMoney(body: MobileMoneyRequest): Observable<MobileMoneyResponse> {
    if (this.useMock()) return this.mock.createMobileMoneyTransfer(body);
    return this.api.post<MobileMoneyResponse | ApiResponse<MobileMoneyResponse>>('/mobile-money/transfers', body).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.warn('[MobileMoney] POST /mobile-money/transfers indisponible:', err?.status);
        if (environment.allowMockFallback) return this.mock.createMobileMoneyTransfer(body);
        throw err;
      })
    );
  }
  mobileMoneyCallback(id: number): Observable<MobileMoneyResponse> {
    if (this.useMock()) return of({ id, transferReference: '', operator: 'ORANGE_MONEY' as const, status: 'CONFIRMED' as const, reconciliationStatus: 'NOT_RECONCILED' as const, operatorTransactionReference: '' });
    return this.api.patch<MobileMoneyResponse | ApiResponse<MobileMoneyResponse>>(`/mobile-money/transfers/${id}/simulate-callback`).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.warn('[MobileMoney] PATCH /mobile-money/transfers/{id}/simulate-callback indisponible:', err?.status);
        if (environment.allowMockFallback) return of({ id, transferReference: '', operator: 'ORANGE_MONEY' as const, status: 'CONFIRMED' as const, reconciliationStatus: 'NOT_RECONCILED' as const, operatorTransactionReference: '' } as MobileMoneyResponse);
        throw err;
      })
    );
  }
  mobileMoneyReconcile(id: number): Observable<MobileMoneyResponse> {
    if (this.useMock()) return of({ id, transferReference: '', operator: 'ORANGE_MONEY' as const, status: 'CONFIRMED' as const, reconciliationStatus: 'RECONCILED' as const, operatorTransactionReference: '' });
    return this.api.post<MobileMoneyResponse | ApiResponse<MobileMoneyResponse>>('/mobile-money/reconciliation', { id }).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.warn('[MobileMoney] POST /mobile-money/reconciliation indisponible:', err?.status);
        if (environment.allowMockFallback) return of({ id, transferReference: '', operator: 'ORANGE_MONEY' as const, status: 'CONFIRMED' as const, reconciliationStatus: 'RECONCILED' as const, operatorTransactionReference: '' } as MobileMoneyResponse);
        throw err;
      })
    );
  }
  chatbot(body: ChatbotRequest): Observable<ChatbotResponse> {
    console.log('[Chatbot] Sending to backend:', environment.apiBaseUrl + '/chatbot', body);
    return this.api.post<ChatbotResponse | ApiResponse<ChatbotResponse>>('/chatbot', body).pipe(
      map(r => {
        console.log('[Chatbot] Backend response:', r);
        return this.unwrap(r);
      }),
      catchError(err => {
        console.warn('[Chatbot] Backend error - status:', err?.status, 'message:', err?.message);
        if (environment.allowMockFallback) {
          console.log('[Chatbot] Falling back to mock');
          return this.mock.processChatbotMessage(body);
        }
        throw err;
      })
    );
  }
}
