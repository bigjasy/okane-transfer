import { Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiService } from './api.service';
import { MockApiService } from './mock-api.service';
import { ApiResponse, BackendPage, PageResponse } from '../models/common.models';
import { UserCreateRequest, UserProfileResponse, UserStatusUpdateRequest, UserSummaryResponse, UserUpdateRequest } from '../models/user.models';
import { AgencyRequest, AgencyResponse, AgencyStaffResponse, CorridorRequest, CorridorResponse, FeeGridRequest, FeeGridResponse, FeeSimulationRequest, FeeSimulationResponse } from '../models/agency.models';
import { ConversionRequest, ConversionResponse, CountryRequest, CountryResponse, CurrencyResponse, ExchangeRateHistoryResponse, ExchangeRateResponse } from '../models/referential.models';
import { AmlAlertResponse, AmlReviewRequest, ComplianceSummaryResponse, KycDocumentResponse, KycDocumentUploadRequest, KycReviewRequest } from '../models/compliance.models';
import { TransferCreateRequest, TransferResponse, PayoutConfirmRequest, PayoutReceiptResponse, PayoutResponse, PayoutSearchRequest, PayoutSearchResponse, PayoutValidateRequest, PayoutValidateResponse, BeneficiaryRequest, BeneficiaryResponse, TransferTrackingResponse } from '../models/transfer.models';
import { CashClosingRequest, CashMovementRequest, CashMovementResponse, CashRegisterOpenRequest, CashRegisterResponse, CommissionResponse } from '../models/finance.models';
import { AuditLogResponse, ChatbotRequest, ChatbotResponse, MobileMoneyReconciliationRequest, MobileMoneyReconciliationResponse, MobileMoneyRequest, MobileMoneyResponse, NotificationPreferencesResponse, NotificationResponse } from '../models/notification.models';
import { AgencyReportResponse, CommissionsReportResponse, TransfersReportResponse } from '../models/report.models';
import { KycDocumentType, MobileMoneyOperator, UserStatus } from '../models/enums';

type BeneficiaryListBackendResponse = BackendPage<BeneficiaryResponse> | PageResponse<BeneficiaryResponse> | BeneficiaryResponse[];
type AuditLogListBackendResponse = BackendPage<AuditLogResponse> | PageResponse<AuditLogResponse> | AuditLogResponse[];
type FeeGridListBackendResponse = BackendPage<FeeGridResponse> | PageResponse<FeeGridResponse> | FeeGridResponse[];

interface PagedResult<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface AuditLogsResult {
  logs: AuditLogResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  backendUnavailable: boolean;
}

interface AuditLogResult {
  log: AuditLogResponse;
  backendUnavailable: boolean;
}

interface FeeGridListResult {
  feeGrids: FeeGridResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  backendUnavailable: boolean;
}

@Injectable({ providedIn: 'root' })
export class DataService {
  constructor(private readonly api: ApiService, private readonly mock: MockApiService) {}
  private useMock(): boolean { return environment.useMockApi; }
  private fallback<T>(obs: Observable<T>, fb: Observable<T>): Observable<T> { return environment.allowMockFallback ? obs.pipe(catchError(err => { console.warn('Backend indisponible, fallback mock:', err); return fb; })) : obs; }
  private pageToList<T>(page: BackendPage<T> | PageResponse<T> | T[]): T[] { return Array.isArray(page) ? page : page.content; }
  private responseToList<T>(response: BackendPage<T> | PageResponse<T> | T[] | ApiResponse<BackendPage<T> | PageResponse<T> | T[]>): T[] {
    return this.pageToList(this.unwrap(response));
  }
  private unwrap<T>(r: T | ApiResponse<T>): T { return r && typeof r === 'object' && 'data' in r && (r as ApiResponse<T>).data ? (r as ApiResponse<T>).data as T : r as T; }
  private pageToResult<T>(response: BackendPage<T> | PageResponse<T> | T[], fallbackPage: number, fallbackSize: number): PagedResult<T> {
    const safePage = Math.max(fallbackPage, 0);
    const safeSize = Math.max(fallbackSize, 1);

    if (Array.isArray(response)) {
      const start = safePage * safeSize;
      return {
        content: response.slice(start, start + safeSize),
        page: safePage,
        size: safeSize,
        totalElements: response.length,
        totalPages: response.length ? Math.ceil(response.length / safeSize) : 0
      };
    }

    const content = response.content ?? [];
    const page = 'page' in response && typeof response.page === 'number'
      ? response.page
      : 'number' in response && typeof response.number === 'number'
        ? response.number
        : safePage;
    const size = typeof response.size === 'number' && response.size > 0 ? response.size : safeSize;
    const totalElements = typeof response.totalElements === 'number' ? response.totalElements : content.length;
    const totalPages = typeof response.totalPages === 'number' ? response.totalPages : totalElements ? Math.ceil(totalElements / size) : 0;
    return { content, page, size, totalElements, totalPages };
  }

  dashboard(role: string): Observable<any> {
    return this.api.get<any>(`/dashboards/${role}`).pipe(
      catchError(err => {
        console.error(`[Dashboard] GET /dashboards/${role} failed:`, err?.status);
        throw err;
      })
    );
  }
  users(page = 0, size = 20): Observable<PagedResult<UserSummaryResponse>> {
    return this.api.get<BackendPage<UserSummaryResponse> | UserSummaryResponse[]>('/users', { page, size }).pipe(
      map(response => this.pageToResult(response, page, size))
    );
  }
  createUser(body: UserCreateRequest): Observable<UserSummaryResponse> { return this.useMock() ? this.mock.createUser(body) : this.fallback(this.api.post<UserSummaryResponse>('/users', body), this.mock.createUser(body)); }
  updateUserStatus(id: number, status: UserStatus): Observable<UserSummaryResponse> {
    const body: UserStatusUpdateRequest = { status };
    if (this.useMock()) return this.mock.updateUserStatus(id, body);

    const request$ = this.api.patch<UserSummaryResponse>(`/users/${id}/status`, body);
    return environment.allowMockFallback
      ? request$.pipe(catchError(err => { console.warn('Backend indisponible, fallback mock:', err); return this.mock.updateUserStatus(id, body); }))
      : request$;
  }
  countries(): Observable<CountryResponse[]> { return this.useMock() ? this.mock.getCountries() : this.fallback(this.api.get<CountryResponse[]>('/countries'), this.mock.getCountries()); }
  createCountry(body: CountryRequest): Observable<CountryResponse> {
    return this.api.post<CountryResponse | ApiResponse<CountryResponse>>('/countries', body).pipe(map(r => this.unwrap(r)));
  }
  updateCountryActivation(id: number, active: boolean): Observable<CountryResponse> {
    return this.api.patch<CountryResponse | ApiResponse<CountryResponse>>(`/countries/${id}/activation`, { active }).pipe(map(r => this.unwrap(r)));
  }
  currencies(): Observable<CurrencyResponse[]> { return this.useMock() ? this.mock.getCurrencies() : this.fallback(this.api.get<CurrencyResponse[]>('/currencies'), this.mock.getCurrencies()); }
  updateCurrencyActivation(id: number, active: boolean): Observable<CurrencyResponse> {
    return this.api.patch<CurrencyResponse | ApiResponse<CurrencyResponse>>(`/currencies/${id}/activation`, { active }).pipe(map(r => this.unwrap(r)));
  }
  rates(): Observable<ExchangeRateResponse[]> { return this.useMock() ? this.mock.getExchangeRates() : this.fallback(this.api.get<ExchangeRateResponse[]>('/exchange-rates'), this.mock.getExchangeRates()); }
  convert(body: ConversionRequest): Observable<ConversionResponse> { return this.useMock() ? this.mock.convertCurrency(body) : this.fallback(this.api.post<ConversionResponse>('/exchange-rates/convert', body), this.mock.convertCurrency(body)); }
  exchangeRateHistory(source?: string, target?: string): Observable<ExchangeRateHistoryResponse[]> {
    const params: Record<string, string> = {};
    if (source) params['source'] = source;
    if (target) params['target'] = target;
    return this.useMock() ? of([]) : this.fallback(
      this.api.get<ExchangeRateHistoryResponse[]>('/exchange-rates/history', params),
      of([])
    );
  }
  syncExternalRates(): Observable<import('../models/compliance.models').ExchangeRateSyncResponse> {
    return this.useMock() ? of({ provider: 'MockFX', source: 'EXTERNAL_API', syncedAt: new Date().toISOString(), updatedCount: 0, rates: [] }) : this.fallback(
      this.api.post<import('../models/compliance.models').ExchangeRateSyncResponse>('/exchange-rates/sync-external', {}),
      of({ provider: 'MockFX', source: 'EXTERNAL_API', syncedAt: new Date().toISOString(), updatedCount: 0, rates: [] })
    );
  }
  agencies(page = 0, size = 50): Observable<PagedResult<AgencyResponse>> {
    return this.api.get<BackendPage<AgencyResponse> | AgencyResponse[]>('/agencies', { page, size }).pipe(
      map(response => this.pageToResult(response, page, size))
    );
  }
  createAgency(body: AgencyRequest): Observable<AgencyResponse> {
    return this.api.post<AgencyResponse | ApiResponse<AgencyResponse>>('/agencies', body).pipe(map(r => this.unwrap(r)));
  }
  updateAgencyStatus(id: number, status: string): Observable<AgencyResponse> {
    return this.api.patch<AgencyResponse | ApiResponse<AgencyResponse>>(`/agencies/${id}/status`, {}, { status }).pipe(map(r => this.unwrap(r)));
  }
  updateAgency(id: number, body: AgencyRequest): Observable<AgencyResponse> {
    return this.api.put<AgencyResponse | ApiResponse<AgencyResponse>>(`/agencies/${id}`, body).pipe(map(r => this.unwrap(r)));
  }
  assignAgentToAgency(agencyId: number, agentId: number): Observable<AgencyResponse> {
    return this.api.post<AgencyResponse | ApiResponse<AgencyResponse>>(`/agencies/${agencyId}/agents/${agentId}`, {}).pipe(map(r => this.unwrap(r)));
  }
  assignManagerToAgency(agencyId: number, managerId: number): Observable<AgencyResponse> {
    return this.api.post<AgencyResponse | ApiResponse<AgencyResponse>>(`/agencies/${agencyId}/managers/${managerId}`, {}).pipe(map(r => this.unwrap(r)));
  }
  agencyStaff(agencyId: number): Observable<AgencyStaffResponse> {
    return this.api.get<AgencyStaffResponse | ApiResponse<AgencyStaffResponse>>(`/agencies/${agencyId}/staff`).pipe(map(r => this.unwrap(r)));
  }
  agencyById(id: number): Observable<AgencyResponse> {
    return this.api.get<AgencyResponse | ApiResponse<AgencyResponse>>(`/agencies/${id}`).pipe(map(r => this.unwrap(r)));
  }
  corridors(): Observable<CorridorResponse[]> { return this.useMock() ? this.mock.getCorridors() : this.fallback(this.api.get<CorridorResponse[]>('/corridors'), this.mock.getCorridors()); }
  createCorridor(body: CorridorRequest): Observable<CorridorResponse> {
    return this.api.post<CorridorResponse | ApiResponse<CorridorResponse>>('/corridors', body).pipe(map(r => this.unwrap(r)));
  }
  updateCorridor(id: number, body: CorridorRequest): Observable<CorridorResponse> {
    return this.api.put<CorridorResponse | ApiResponse<CorridorResponse>>(`/corridors/${id}`, body).pipe(map(r => this.unwrap(r)));
  }
  toggleCorridorActivation(id: number, active: boolean): Observable<CorridorResponse> {
    return this.api.patch<CorridorResponse | ApiResponse<CorridorResponse>>(`/corridors/${id}/activation`, { active }).pipe(map(r => this.unwrap(r)));
  }
  createFeeGrid(body: FeeGridRequest): Observable<FeeGridResponse> {
    return this.api.post<FeeGridResponse | ApiResponse<FeeGridResponse>>('/fee-grids', body).pipe(map(r => this.unwrap(r)));
  }
  updateFeeGrid(id: number, body: FeeGridRequest): Observable<FeeGridResponse> {
    return this.api.put<FeeGridResponse | ApiResponse<FeeGridResponse>>(`/fee-grids/${id}`, body).pipe(map(r => this.unwrap(r)));
  }
  toggleFeeGridActivation(id: number, active: boolean): Observable<FeeGridResponse> {
    return this.api.patch<FeeGridResponse | ApiResponse<FeeGridResponse>>(`/fee-grids/${id}/activation`, { active }).pipe(map(r => this.unwrap(r)));
  }
  exportFeeGridsCsv(corridorId?: number): Observable<Blob> {
    const params: Record<string, string | number> = { format: 'CSV' };
    if (corridorId != null) params['corridorId'] = corridorId;
    return this.api.getBlob('/fee-grids/export', params);
  }
  commissions(page = 0, size = 20, agencyId?: number): Observable<PagedResult<CommissionResponse>> {
    const params: Record<string, string | number> = { page, size };
    if (agencyId != null) params['agencyId'] = agencyId;
    return this.api.get<BackendPage<CommissionResponse> | PageResponse<CommissionResponse>>('/commissions', params).pipe(
      map(response => this.pageToResult(this.unwrap(response), page, size))
    );
  }
  agencyCashRegisters(agencyId: number): Observable<CashRegisterResponse[]> {
    return this.api.get<CashRegisterResponse[] | ApiResponse<CashRegisterResponse[]>>(`/cash-registers/agency/${agencyId}`).pipe(
      map(r => this.pageToList(this.unwrap(r)))
    );
  }
  feeGrids(page = 0, size = 20): Observable<FeeGridListResult> {
    const toResult = (response: FeeGridListBackendResponse, backendUnavailable: boolean): FeeGridListResult => {
      const result = this.pageToResult(response, page, size);
      return {
        feeGrids: result.content,
        page: result.page,
        size: result.size,
        totalElements: result.totalElements,
        totalPages: result.totalPages,
        backendUnavailable
      };
    };

    const mock = (backendUnavailable: boolean): Observable<FeeGridListResult> =>
      this.mock.getFeeGrids().pipe(map(feeGrids => toResult(feeGrids, backendUnavailable)));

    if (this.useMock()) return mock(false);

    const real = this.api.get<FeeGridListBackendResponse | ApiResponse<FeeGridListBackendResponse>>('/fee-grids', { page, size }).pipe(
      map(response => this.unwrap<FeeGridListBackendResponse>(response)),
      map(response => toResult(response, false))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[FeeGrids] GET /fee-grids indisponible:', err?.status); return mock(true); }))
      : real;
  }
  simulateFees(body: FeeSimulationRequest): Observable<FeeSimulationResponse> {
    return this.useMock() ? this.mock.simulateFees(body) : this.fallback(
      this.api.post<FeeSimulationResponse | ApiResponse<FeeSimulationResponse>>('/fee-grids/simulate', body).pipe(map(r => this.unwrap(r))),
      this.mock.simulateFees(body)
    );
  }
  transfers(): Observable<TransferResponse[]> {
    const real = this.api.get<BackendPage<TransferResponse> | PageResponse<TransferResponse> | TransferResponse[] | ApiResponse<BackendPage<TransferResponse> | PageResponse<TransferResponse> | TransferResponse[]>>('/transfers')
      .pipe(map(r => this.responseToList(r)));
    return this.useMock() ? this.mock.getTransfers() : this.fallback(real, this.mock.getTransfers());
  }
  transfer(ref: string): Observable<TransferResponse> { return this.useMock() ? this.mock.getTransferByReference(ref) : this.fallback(this.api.get<TransferResponse>(`/transfers/${ref}`), this.mock.getTransferByReference(ref)); }
  trackTransfer(ref: string): Observable<TransferTrackingResponse> {
    return this.useMock()
      ? this.mock.trackTransfer(ref)
      : this.api.get<TransferTrackingResponse>(`/transfers/track/${encodeURIComponent(ref)}`);
  }
  createTransfer(body: TransferCreateRequest): Observable<TransferResponse> { return this.useMock() ? this.mock.createTransfer(body) : this.fallback(this.api.post<TransferResponse>('/transfers', body), this.mock.createTransfer(body)); }
  confirmTransferPayment(reference: string): Observable<string> {
    if (this.useMock()) return of('123456');
    return this.api.post<string | ApiResponse<string>>(`/transfers/${encodeURIComponent(reference)}/confirm-payment`, {}).pipe(
      map(r => (typeof r === 'string' ? r : this.unwrap(r)))
    );
  }
  cancelTransfer(reference: string): Observable<TransferResponse> {
    if (this.useMock()) return this.mock.getTransferByReference(reference);
    return this.api.patch<TransferResponse | ApiResponse<TransferResponse>>(`/transfers/${encodeURIComponent(reference)}/cancel`, {}).pipe(
      map(r => this.unwrap(r))
    );
  }
  searchPayout(body: PayoutSearchRequest): Observable<PayoutSearchResponse> {
    return this.useMock() ? this.mock.searchPayouts(body) : this.fallback(
      this.api.post<PayoutSearchResponse | ApiResponse<PayoutSearchResponse>>('/payouts/search', body).pipe(map(r => this.unwrap(r))),
      this.mock.searchPayouts(body)
    );
  }
  validatePayout(body: PayoutValidateRequest): Observable<PayoutValidateResponse> {
    return this.useMock() ? this.mock.validatePayout(body) : this.fallback(
      this.api.post<PayoutValidateResponse | ApiResponse<PayoutValidateResponse>>('/payouts/validate', body).pipe(map(r => this.unwrap(r))),
      this.mock.validatePayout(body)
    );
  }
  confirmPayout(body: PayoutConfirmRequest): Observable<PayoutResponse> {
    return this.useMock() ? this.mock.confirmPayout(body) : this.fallback(
      this.api.post<PayoutResponse | ApiResponse<PayoutResponse>>('/payouts/confirm', body).pipe(map(r => this.unwrap(r))),
      this.mock.confirmPayout(body)
    );
  }
  payoutReceipt(transferReference: string): Observable<PayoutReceiptResponse> {
    return this.useMock() ? this.mock.payoutReceipt(transferReference) : this.fallback(
      this.api.get<PayoutReceiptResponse | ApiResponse<PayoutReceiptResponse>>(`/payouts/${encodeURIComponent(transferReference)}/receipt`).pipe(map(r => this.unwrap(r))),
      this.mock.payoutReceipt(transferReference)
    );
  }
  currentCash(): Observable<CashRegisterResponse> { return this.useMock() ? this.mock.getCurrentCashRegister() : this.fallback(this.api.get<CashRegisterResponse>('/cash-registers/current', { currencyCode: 'MAD' }), this.mock.getCurrentCashRegister()); }
  openCash(body: CashRegisterOpenRequest): Observable<CashRegisterResponse> { return this.useMock() ? this.mock.openCashRegister(body) : this.fallback(this.api.post<CashRegisterResponse>('/cash-registers/open', body), this.mock.openCashRegister(body)); }
  addMovement(id: number, body: CashMovementRequest): Observable<CashMovementResponse> {
    return this.useMock() ? this.mock.addCashMovement(body) : this.fallback(
      this.api.post<CashMovementResponse | ApiResponse<CashMovementResponse>>(`/cash-registers/${id}/movements`, body).pipe(map(r => this.unwrap(r))),
      this.mock.addCashMovement(body)
    );
  }
  closeCash(id: number, body: CashClosingRequest): Observable<CashRegisterResponse> { return this.useMock() ? this.mock.closeCashRegister(body) : this.fallback(this.api.post<CashRegisterResponse>(`/cash-registers/${id}/close`, body), this.mock.closeCashRegister(body)); }
  movements(id = 1): Observable<CashMovementResponse[]> {
    const real = this.api.get<BackendPage<CashMovementResponse> | PageResponse<CashMovementResponse> | CashMovementResponse[] | ApiResponse<BackendPage<CashMovementResponse> | PageResponse<CashMovementResponse> | CashMovementResponse[]>>(`/cash-registers/${id}/movements`)
      .pipe(map(r => this.responseToList(r)));
    return this.useMock() ? this.mock.getCashMovements() : this.fallback(real, this.mock.getCashMovements());
  }
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
  beneficiaries(): Observable<{ beneficiaries: BeneficiaryResponse[]; backendUnavailable: boolean }> {
    const mock = (backendUnavailable: boolean): Observable<{ beneficiaries: BeneficiaryResponse[]; backendUnavailable: boolean }> =>
      this.mock.getBeneficiaries().pipe(map(beneficiaries => ({ beneficiaries, backendUnavailable })));
    if (this.useMock()) return mock(false);

    const real = this.api.get<BeneficiaryListBackendResponse | ApiResponse<BeneficiaryListBackendResponse>>('/beneficiaries').pipe(
      map(response => this.unwrap<BeneficiaryListBackendResponse>(response)),
      map(response => ({ beneficiaries: this.pageToList(response), backendUnavailable: false }))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[Beneficiaries] GET /beneficiaries indisponible:', err?.status); return mock(true); }))
      : real;
  }
  createBeneficiary(body: BeneficiaryRequest): Observable<{ beneficiary: BeneficiaryResponse; backendUnavailable: boolean }> {
    const mock = (backendUnavailable: boolean): Observable<{ beneficiary: BeneficiaryResponse; backendUnavailable: boolean }> =>
      this.mock.createBeneficiary(body).pipe(map(beneficiary => ({ beneficiary, backendUnavailable })));
    if (this.useMock()) return mock(false);

    const real = this.api.post<BeneficiaryResponse | ApiResponse<BeneficiaryResponse>>('/beneficiaries', body).pipe(
      map(response => ({ beneficiary: this.unwrap<BeneficiaryResponse>(response), backendUnavailable: false }))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[Beneficiaries] POST /beneficiaries indisponible:', err?.status); return mock(true); }))
      : real;
  }
  updateBeneficiary(id: number, body: BeneficiaryRequest): Observable<{ beneficiary: BeneficiaryResponse; backendUnavailable: boolean }> {
    const mock = (backendUnavailable: boolean): Observable<{ beneficiary: BeneficiaryResponse; backendUnavailable: boolean }> =>
      this.mock.updateBeneficiary(id, body).pipe(map(beneficiary => ({ beneficiary, backendUnavailable })));
    if (this.useMock()) return mock(false);

    const real = this.api.put<BeneficiaryResponse | ApiResponse<BeneficiaryResponse>>(`/beneficiaries/${id}`, body).pipe(
      map(response => ({ beneficiary: this.unwrap<BeneficiaryResponse>(response), backendUnavailable: false }))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[Beneficiaries] PUT /beneficiaries/{id} indisponible:', err?.status); return mock(true); }))
      : real;
  }
  deleteBeneficiary(id: number): Observable<{ backendUnavailable: boolean }> {
    const mock = (backendUnavailable: boolean): Observable<{ backendUnavailable: boolean }> =>
      this.mock.deleteBeneficiary(id).pipe(map(() => ({ backendUnavailable })));
    if (this.useMock()) return mock(false);

    const real = this.api.delete<void>(`/beneficiaries/${id}`).pipe(
      map(() => ({ backendUnavailable: false }))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[Beneficiaries] DELETE /beneficiaries/{id} indisponible:', err?.status); return mock(true); }))
      : real;
  }
  kycDocuments(): Observable<KycDocumentResponse[]> {
    return this.pendingKycDocuments().pipe(map(result => result.documents));
  }
  clientKycDocuments(): Observable<{ documents: KycDocumentResponse[]; backendUnavailable: boolean }> {
    const mock = (backendUnavailable: boolean): Observable<{ documents: KycDocumentResponse[]; backendUnavailable: boolean }> =>
      this.mock.clientKycDocuments().pipe(map(documents => ({ documents, backendUnavailable })));
    if (this.useMock()) return mock(false);

    const real = this.api.get<KycDocumentResponse[] | ApiResponse<KycDocumentResponse[]>>('/kyc/documents/me').pipe(
      map(response => ({ documents: this.unwrap<KycDocumentResponse[]>(response), backendUnavailable: false }))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[KYC] GET /kyc/documents/me indisponible:', err?.status); return mock(true); }))
      : real;
  }
  uploadKycDocument(formData: FormData): Observable<KycDocumentResponse> {
    const documentTypes: KycDocumentType[] = ['CIN_FRONT', 'CIN_BACK', 'PASSPORT', 'PROOF_OF_ADDRESS'];
    const typeValue = formData.get('documentType');
    const documentType = typeof typeValue === 'string' && documentTypes.includes(typeValue as KycDocumentType)
      ? typeValue as KycDocumentType
      : 'PASSPORT';
    const mock = of({ id: Date.now(), documentType, status: 'PENDING' as const, uploadedAt: new Date().toISOString() });
    return this.useMock() ? mock : this.fallback(
      this.api.post<KycDocumentResponse | ApiResponse<KycDocumentResponse>>('/kyc/documents', formData).pipe(map(r => this.unwrap(r))),
      mock
    );
  }
  uploadClientKycDocument(body: KycDocumentUploadRequest): Observable<{ document: KycDocumentResponse; backendUnavailable: boolean }> {
    const mock = (backendUnavailable: boolean): Observable<{ document: KycDocumentResponse; backendUnavailable: boolean }> =>
      this.mock.uploadClientKycDocument(body).pipe(map(document => ({ document, backendUnavailable })));
    if (this.useMock()) return mock(false);

    const formData = new FormData();
    formData.append('file', body.file);
    formData.append('documentType', body.documentType);
    formData.append('documentNumber', body.documentNumber);

    const real = this.api.post<KycDocumentResponse | ApiResponse<KycDocumentResponse>>('/kyc/documents', formData).pipe(
      map(response => ({ document: this.unwrap<KycDocumentResponse>(response), backendUnavailable: false }))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[KYC] POST /kyc/documents indisponible:', err?.status); return mock(true); }))
      : real;
  }
  pendingKycDocuments(page = 0, size = 20): Observable<{ documents: KycDocumentResponse[]; backendUnavailable: boolean }> {
    const mock = (backendUnavailable: boolean): Observable<{ documents: KycDocumentResponse[]; backendUnavailable: boolean }> =>
      this.mock.kycDocuments().pipe(map(documents => ({ documents, backendUnavailable })));
    if (this.useMock()) return mock(false);

    const real = this.api.get<BackendPage<KycDocumentResponse> | PageResponse<KycDocumentResponse> | KycDocumentResponse[] | ApiResponse<BackendPage<KycDocumentResponse> | PageResponse<KycDocumentResponse> | KycDocumentResponse[]>>('/kyc/pending', { page, size }).pipe(
      map(response => ({ documents: this.responseToList(response), backendUnavailable: false }))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[KYC] GET /kyc/pending indisponible:', err?.status); return mock(true); }))
      : real;
  }
  reviewKycDocument(id: number, body: KycReviewRequest): Observable<{ document: KycDocumentResponse; backendUnavailable: boolean }> {
    const mock = (backendUnavailable: boolean): Observable<{ document: KycDocumentResponse; backendUnavailable: boolean }> =>
      this.mock.reviewKycDocument(id, body).pipe(map(document => ({ document, backendUnavailable })));
    if (this.useMock()) return mock(false);

    const real = this.api.patch<KycDocumentResponse | ApiResponse<KycDocumentResponse>>(`/kyc/documents/${id}/review`, body).pipe(
      map(response => ({ document: this.unwrap<KycDocumentResponse>(response), backendUnavailable: false }))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[KYC] PATCH /kyc/documents/{id}/review indisponible:', err?.status); return mock(true); }))
      : real;
  }
  amlAlerts(): Observable<AmlAlertResponse[]> { return this.useMock() ? this.mock.amlAlerts() : this.fallback(this.api.get<PageResponse<AmlAlertResponse>>('/aml/alerts').pipe((source) => new Observable<AmlAlertResponse[]>(sub => source.subscribe({ next: v => { sub.next(v.content); sub.complete(); }, error: e => sub.error(e) }))), this.mock.amlAlerts()); }
  reviewAmlAlert(id: number, body: AmlReviewRequest): Observable<AmlAlertResponse> {
    return this.useMock() ? this.mock.amlAlerts().pipe(map(alerts => alerts[0])) : this.fallback(
      this.api.patch<AmlAlertResponse | ApiResponse<AmlAlertResponse>>(`/aml/alerts/${id}/review`, body).pipe(map(r => this.unwrap(r))),
      this.mock.amlAlerts().pipe(map(alerts => alerts[0]))
    );
  }
  complianceSummary(): Observable<ComplianceSummaryResponse> {
    return this.useMock() ? of({ pendingKycDocuments: 0, openAmlAlerts: 0, criticalAmlAlerts: 0, activeWatchlistEntries: 0, blockedTransfers: 0 }) : this.fallback(
      this.api.get<ComplianceSummaryResponse>('/aml/compliance-summary'),
      of({ pendingKycDocuments: 0, openAmlAlerts: 0, criticalAmlAlerts: 0, activeWatchlistEntries: 0, blockedTransfers: 0 })
    );
  }
  notifications(page = 0, size = 20): Observable<NotificationResponse[]> {
    if (this.useMock()) return this.mock.notifications();
    return this.api.get<PageResponse<NotificationResponse> | ApiResponse<PageResponse<NotificationResponse>>>(`/notifications/me`, { page, size }).pipe(
      map(r => this.pageToList(this.unwrap(r))),
      catchError(err => {
        console.warn('[Notifications] GET /notifications/me indisponible:', err?.status);
        if (environment.allowMockFallback && (!err?.status || err.status >= 500)) return this.mock.notifications();
        throw err;
      })
    );
  }
  notificationPreferences(): Observable<NotificationPreferencesResponse> {
    if (this.useMock()) return of({ email: true, sms: false, push: false });
    return this.api.get<NotificationPreferencesResponse | ApiResponse<NotificationPreferencesResponse>>('/notifications/preferences').pipe(
      map(r => this.unwrap(r))
    );
  }
  updateNotificationPreferences(body: NotificationPreferencesResponse): Observable<NotificationPreferencesResponse> {
    if (this.useMock()) return of(body);
    return this.api.put<NotificationPreferencesResponse | ApiResponse<NotificationPreferencesResponse>>('/notifications/preferences', body).pipe(
      map(r => this.unwrap(r))
    );
  }
  transfersReport(params: { format?: string; agencyId?: number } = {}): Observable<TransfersReportResponse> {
    return this.api.get<TransfersReportResponse | ApiResponse<TransfersReportResponse>>('/reports/transfers', params).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.error('[Reports] GET /reports/transfers failed:', err?.status, err?.error);
        throw err;
      })
    );
  }
  agencyReport(agencyId: number, format = 'JSON'): Observable<AgencyReportResponse> {
    return this.api.get<AgencyReportResponse | ApiResponse<AgencyReportResponse>>(`/reports/agencies/${agencyId}`, { format }).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.error('[Reports] GET /reports/agencies failed:', err?.status, err?.error);
        throw err;
      })
    );
  }
  commissionsReport(params: { format?: string; agencyId?: number } = {}): Observable<CommissionsReportResponse> {
    return this.api.get<CommissionsReportResponse | ApiResponse<CommissionsReportResponse>>('/reports/commissions', params).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.error('[Reports] GET /reports/commissions failed:', err?.status, err?.error);
        throw err;
      })
    );
  }
  transfersReportBlob(params: { format?: string; agencyId?: number } = {}): Observable<Blob> {
    return this.api.getBlob('/reports/transfers', params);
  }
  commissionsReportBlob(params: { format?: string; agencyId?: number } = {}): Observable<Blob> {
    return this.api.getBlob('/reports/commissions', params);
  }
  sendReceiptBlob(reference: string, withdrawalCode?: string): Observable<Blob> {
    const params: Record<string, string> = { format: 'PDF' };
    if (withdrawalCode) params['withdrawalCode'] = withdrawalCode;
    return this.api.getBlob(`/transfers/${encodeURIComponent(reference)}/receipt`, params);
  }
  payoutReceiptBlob(transferReference: string): Observable<Blob> {
    return this.api.getBlob(`/payouts/${encodeURIComponent(transferReference)}/receipt`, { format: 'PDF' });
  }
  markNotificationRead(id: number): Observable<NotificationResponse> {
    return this.useMock() ? this.mock.notifications().pipe(map(items => items[0])) : this.fallback(
      this.api.patch<NotificationResponse | ApiResponse<NotificationResponse>>(`/notifications/${id}/read`, {}).pipe(map(r => this.unwrap(r))),
      this.mock.notifications().pipe(map(items => items[0]))
    );
  }
  auditLogs(page = 0, size = 20, actionQuery = ''): Observable<AuditLogsResult> {
    const params: Record<string, string | number> = { page, size };
    if (actionQuery.trim()) params['actionQuery'] = actionQuery.trim();

    const toResult = (response: AuditLogListBackendResponse, backendUnavailable: boolean): AuditLogsResult => {
      const result = this.pageToResult(response, page, size);
      return {
        logs: result.content,
        page: result.page,
        size: result.size,
        totalElements: result.totalElements,
        totalPages: result.totalPages,
        backendUnavailable
      };
    };

    const mock = (backendUnavailable: boolean): Observable<AuditLogsResult> =>
      this.mock.auditLogs().pipe(map(logs => toResult(logs, backendUnavailable)));

    if (this.useMock()) return mock(false);

    const real = this.api.get<AuditLogListBackendResponse | ApiResponse<AuditLogListBackendResponse>>('/audit-logs', params).pipe(
      map(response => this.unwrap<AuditLogListBackendResponse>(response)),
      map(response => toResult(response, false))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[Audit] GET /audit-logs indisponible:', err?.status); return mock(true); }))
      : real;
  }
  auditLog(id: number): Observable<AuditLogResult> {
    const mock = (backendUnavailable: boolean): Observable<AuditLogResult> =>
      this.mock.auditLog(id).pipe(map(log => ({ log, backendUnavailable })));

    if (this.useMock()) return mock(false);

    const real = this.api.get<AuditLogResponse | ApiResponse<AuditLogResponse>>(`/audit-logs/${id}`).pipe(
      map(response => ({ log: this.unwrap<AuditLogResponse>(response), backendUnavailable: false }))
    );

    return environment.allowMockFallback
      ? real.pipe(catchError(err => { console.warn('[Audit] GET /audit-logs/{id} indisponible:', err?.status); return mock(true); }))
      : real;
  }
  mobileMoney(body: MobileMoneyRequest): Observable<MobileMoneyResponse> {
    if (this.useMock()) return this.mock.createMobileMoneyTransfer(body);
    return this.api.post<MobileMoneyResponse | ApiResponse<MobileMoneyResponse>>('/mobile-money/transfers', body).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.warn('[MobileMoney] POST /mobile-money/transfers indisponible:', err?.status);
        if (environment.allowMockFallback && (!err?.status || err.status >= 500)) {
          return this.mock.createMobileMoneyTransfer(body);
        }
        throw err;
      })
    );
  }
  mobileMoneyCallback(id: number): Observable<MobileMoneyResponse> {
    if (this.useMock()) return this.mock.mobileMoneySimulateCallback(id);
    return this.api.patch<MobileMoneyResponse | ApiResponse<MobileMoneyResponse>>(`/mobile-money/transfers/${id}/simulate-callback`).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.warn('[MobileMoney] PATCH /mobile-money/transfers/{id}/simulate-callback indisponible:', err?.status);
        if (environment.allowMockFallback && (!err?.status || err.status >= 500)) {
          return this.mock.mobileMoneySimulateCallback(id);
        }
        throw err;
      })
    );
  }
  mobileMoneyReconcile(operator: MobileMoneyOperator, date = new Date().toISOString().slice(0, 10)): Observable<MobileMoneyReconciliationResponse> {
    const body: MobileMoneyReconciliationRequest = { operator, date };
    if (this.useMock()) return of({ reconciled: 1, mismatches: 0 });
    return this.api.post<MobileMoneyReconciliationResponse | ApiResponse<MobileMoneyReconciliationResponse>>('/mobile-money/reconciliation', body).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.warn('[MobileMoney] POST /mobile-money/reconciliation indisponible:', err?.status);
        if (environment.allowMockFallback) return of({ reconciled: 1, mismatches: 0 });
        throw err;
      })
    );
  }
  mobileMoneyList(page = 0, size = 20): Observable<MobileMoneyResponse[]> {
    if (this.useMock()) return this.mock.mobileMoneyList();
    return this.api.get<PageResponse<MobileMoneyResponse> | ApiResponse<PageResponse<MobileMoneyResponse>>>('/mobile-money/transfers', { page, size }).pipe(
      map(r => this.pageToList(this.unwrap(r))),
      catchError(err => {
        console.warn('[MobileMoney] GET /mobile-money/transfers indisponible:', err?.status);
        if (environment.allowMockFallback && (!err?.status || err.status >= 500)) {
          return this.mock.mobileMoneyList();
        }
        throw err;
      })
    );
  }
  chatbot(body: ChatbotRequest): Observable<ChatbotResponse> {
    if (this.useMock()) return this.mock.processChatbotMessage(body);
    return this.api.post<ChatbotResponse | ApiResponse<ChatbotResponse>>('/chatbot', body).pipe(
      map(r => this.unwrap(r)),
      catchError(err => {
        console.warn('[Chatbot] POST /chatbot indisponible:', err?.status);
        if (environment.allowMockFallback) return this.mock.processChatbotMessage(body);
        throw err;
      })
    );
  }
}
