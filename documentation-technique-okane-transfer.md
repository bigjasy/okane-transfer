# Documentation technique — OkaneTransfer

## 1. Introduction et contexte du projet

OkaneTransfer est une application web monolithique de gestion des transferts d’argent, inspirée des plateformes de type Western Union ou MoneyGram. Le projet vise à couvrir le cycle complet d’un transfert : configuration des pays, devises, agences, corridors et frais ; création d’un transfert par un agent ; paiement au bénéficiaire ; gestion de caisse ; suivi client ; conformité KYC/AML ; reporting et audit.

Le projet doit rester une application monolithique en couches, sans microservices, développée avec une configuration Spring manuelle, sans Spring Boot. La plateforme expose une API REST sécurisée consommée par une SPA Angular 17+.

### Stack imposée

| Couche | Technologie |
|---|---|
| Backend | Spring MVC 6, Spring Security 6 |
| Persistance | Spring Data JPA, Hibernate |
| Base de données | PostgreSQL ou MySQL |
| Documentation API | Swagger / OpenAPI 3 avec SpringDoc configuré manuellement |
| Frontend | Angular 17+, Chart.js |
| Sécurité | JWT, refresh token, BCrypt, 2FA OTP, AES-256, rate limiting |

---

## 2. Objectifs fonctionnels et techniques

### 2.1 Objectifs fonctionnels

- Permettre l’envoi et la réception de fonds entre particuliers.
- Gérer les transferts nationaux et internationaux.
- Gérer les pays, devises, taux de change et historiques de taux.
- Gérer les agences, responsables d’agence, agents et clients.
- Gérer les corridors de transfert entre pays.
- Paramétrer des grilles de frais par corridor, devise, tranche de montant et période de validité.
- Calculer automatiquement les frais, le taux appliqué, le montant reçu et les commissions.
- Générer un code de retrait unique pour chaque transfert validé.
- Permettre le paiement d’un transfert après vérification du code de retrait et de l’identité du bénéficiaire.
- Suivre les mouvements de caisse et clôturer la caisse en fin de journée.
- Fournir des tableaux de bord et rapports financiers.
- Gérer les documents KYC et les alertes AML.
- Simuler des transferts Mobile Money.
- Envoyer des notifications email, SMS simulé ou push simulé.
- Fournir un chatbot FAQ / statut / frais / délais avec escalade simulée.

### 2.2 Objectifs techniques

- Structurer le backend en couches : présentation, service, persistance, infrastructure.
- Centraliser la logique métier dans des services transactionnels.
- Exposer une API REST cohérente et documentable par OpenAPI 3.
- Sécuriser l’accès aux ressources par JWT, refresh token, rôles et guards frontend.
- Journaliser les actions sensibles dans un journal d’audit.
- Chiffrer les données sensibles en base avec AES-256.
- Valider toutes les entrées avec Jakarta Validation.
- Préparer le frontend Angular avec lazy loading, guards, intercepteurs JWT et i18n.

---

## 3. Acteurs et rôles applicatifs

| Acteur | Rôle Spring Security | Responsabilités principales |
|---|---|---|
| Administrateur | `ROLE_ADMIN` | Configuration globale, utilisateurs, devises, pays, frais, agences, supervision, conformité, rapports consolidés, audit |
| Responsable d’agence | `ROLE_MANAGER` | Gestion des agents de son agence, validation d’opérations sensibles, suivi de caisse, rapports d’agence |
| Agent | `ROLE_AGENT` | Création des transferts, vérification d’identité, paiement des retraits, gestion de caisse quotidienne |
| Client | `ROLE_CLIENT` | Suivi de ses transferts, gestion de profil, bénéficiaires, préférences de notification |

---

## 4. Architecture globale monolithique en couches

### 4.1 Vue logique

```text
Angular 17+ SPA
   |
   | HTTPS / JSON / JWT
   v
Spring MVC 6 REST Controllers
   |
   v
Application Services @Transactional
   |
   v
Spring Data JPA Repositories
   |
   v
Hibernate ORM
   |
   v
PostgreSQL ou MySQL
```

### 4.2 Couches backend

| Couche | Responsabilité | Exemples |
|---|---|---|
| Présentation | Exposition REST, validation DTO, mapping HTTP | `AuthController`, `TransferController`, `KycController` |
| Service | Logique métier, transactions, règles financières, conformité | `TransferService`, `FeeCalculationService`, `AmlService` |
| Persistance | Accès aux données via Spring Data JPA | `TransferRepository`, `UserRepository`, `CurrencyRepository` |
| Infrastructure | Sécurité, JWT, CORS, Swagger, exceptions, audit, chiffrement | `SecurityConfig`, `JwtService`, `OpenApiConfig` |

### 4.3 Contraintes d’architecture

- Pas de microservices.
- Pas de Spring Boot.
- Configuration Spring manuelle via `WebApplicationInitializer` ou `web.xml`.
- Les contrôleurs ne contiennent pas de logique métier.
- Les entités JPA ne sont pas exposées directement dans les réponses REST.
- Les DTOs sont utilisés pour toutes les entrées/sorties API.
- Les services modifiant les données sont annotés `@Transactional`.
- Les règles de sécurité sont appliquées au niveau filtre JWT, configuration HTTP et annotations méthode.

---

## 5. Structure recommandée des packages backend

Package racine proposé : `ma.ensam.okanetransfer`

```text
ma.ensam.okanetransfer
├── config
│   ├── WebAppInitializer.java
│   ├── WebMvcConfig.java
│   ├── PersistenceConfig.java
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   ├── CorsConfig.java
│   └── JacksonConfig.java
├── security
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   ├── CustomUserDetailsService.java
│   ├── PasswordConfig.java
│   ├── RateLimitFilter.java
│   ├── RefreshTokenService.java
│   └── TwoFactorService.java
├── domain
│   ├── user
│   │   ├── User.java
│   │   ├── Admin.java
│   │   ├── Manager.java
│   │   ├── Agent.java
│   │   └── Client.java
│   ├── referential
│   │   ├── Country.java
│   │   ├── Currency.java
│   │   ├── ExchangeRate.java
│   │   └── ExchangeRateHistory.java
│   ├── agency
│   │   ├── Agency.java
│   │   ├── Corridor.java
│   │   └── FeeGrid.java
│   ├── transfer
│   │   ├── Beneficiary.java
│   │   ├── Transfer.java
│   │   ├── TransferPayment.java
│   │   └── MobileMoneyTransfer.java
│   ├── finance
│   │   ├── CashRegister.java
│   │   ├── CashMovement.java
│   │   ├── Commission.java
│   │   └── AgencyReport.java
│   ├── compliance
│   │   ├── KycDocument.java
│   │   ├── AmlAlert.java
│   │   └── WatchlistEntry.java
│   ├── notification
│   │   └── Notification.java
│   └── audit
│       └── AuditLog.java
├── enums
├── dto
│   ├── auth
│   ├── user
│   ├── referential
│   ├── agency
│   ├── transfer
│   ├── finance
│   ├── compliance
│   ├── notification
│   ├── dashboard
│   └── common
├── mapper
├── repository
├── service
├── controller
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   ├── ForbiddenOperationException.java
│   └── ErrorResponse.java
└── util
    ├── CodeGenerator.java
    ├── MoneyUtils.java
    ├── AesEncryptionConverter.java
    └── DateUtils.java
```

---

## 6. Structure recommandée du frontend Angular 17+

```text
src/app
├── core
│   ├── guards
│   │   ├── auth.guard.ts
│   │   ├── role.guard.ts
│   │   └── two-factor.guard.ts
│   ├── interceptors
│   │   ├── jwt.interceptor.ts
│   │   ├── refresh-token.interceptor.ts
│   │   └── error.interceptor.ts
│   ├── services
│   │   ├── auth.service.ts
│   │   ├── token.service.ts
│   │   ├── notification.service.ts
│   │   └── api-error.service.ts
│   └── models
├── shared
│   ├── components
│   │   ├── navbar
│   │   ├── sidebar
│   │   ├── confirm-dialog
│   │   ├── data-table
│   │   └── status-badge
│   ├── pipes
│   ├── validators
│   └── directives
├── features
│   ├── auth
│   ├── admin
│   │   ├── users
│   │   ├── agencies
│   │   ├── countries
│   │   ├── currencies
│   │   ├── fee-grids
│   │   ├── compliance
│   │   ├── reports
│   │   └── audit
│   ├── manager
│   ├── agent
│   │   ├── transfers
│   │   ├── payout
│   │   └── cash-register
│   ├── client
│   │   ├── profile
│   │   ├── beneficiaries
│   │   └── transfer-tracking
│   ├── mobile-money
│   └── chatbot
├── layouts
│   ├── public-layout
│   └── secured-layout
├── i18n
│   ├── fr.json
│   ├── en.json
│   └── ar.json
└── app.routes.ts
```

### Principes frontend

- Lazy loading par espace : `admin`, `manager`, `agent`, `client`.
- Guards par rôle sur les routes.
- Intercepteur JWT ajoutant `Authorization: Bearer <token>`.
- Intercepteur refresh token pour renouveler automatiquement l’access token.
- Chart.js pour les tableaux de bord.
- i18n français / anglais / arabe.
- Interface responsive et accessible.

---

## 7. Modèle de données détaillé

### 7.1 Tables principales

| Table | Rôle |
|---|---|
| `users` | Données communes à tous les utilisateurs |
| `admins` | Extension spécifique administrateur |
| `managers` | Extension responsable d’agence |
| `agents` | Extension agent |
| `clients` | Extension client |
| `refresh_tokens` | Refresh tokens persistés et révocables |
| `otp_verifications` | OTP 2FA et opérations sensibles |
| `audit_logs` | Journalisation des actions sensibles |
| `countries` | Pays supportés |
| `currencies` | Devises supportées |
| `exchange_rates` | Taux actifs |
| `exchange_rate_histories` | Historique des taux |
| `agencies` | Agences physiques |
| `corridors` | Couples pays source / pays destination |
| `fee_grids` | Grilles tarifaires |
| `beneficiaries` | Bénéficiaires d’un client ou d’un transfert |
| `transfers` | Transferts d’argent |
| `transfer_payments` | Paiements / retraits des transferts |
| `cash_registers` | Caisses d’agence / agent |
| `cash_movements` | Mouvements de caisse |
| `commissions` | Commissions agence / centrale |
| `agency_reports` | Rapports d’agence |
| `kyc_documents` | Documents KYC |
| `aml_alerts` | Alertes AML |
| `watchlist_entries` | Liste de surveillance fictive OFAC |
| `notifications` | Notifications email/SMS/push simulées |
| `mobile_money_transfers` | Extension transfert Mobile Money simulé |

### 7.2 Relations principales

| Relation | Cardinalité | Description |
|---|---:|---|
| `User` → `RefreshToken` | 1..N | Un utilisateur peut avoir plusieurs refresh tokens actifs ou révoqués |
| `User` → `OtpVerification` | 1..N | OTP pour login ou opération sensible |
| `Agency` → `Agent` | 1..N | Une agence possède plusieurs agents |
| `Agency` → `Manager` | 1..N | Une agence peut avoir un ou plusieurs responsables |
| `Country` → `Currency` | N..1 | Plusieurs pays peuvent partager une devise |
| `Corridor` → `Country` | N..1 source et destination | Un corridor relie deux pays |
| `FeeGrid` → `Corridor` | N..1 | Une grille appartient à un corridor |
| `ExchangeRate` → `Currency` | N..1 source et cible | Taux entre deux devises |
| `Client` → `Beneficiary` | 1..N | Un client peut enregistrer plusieurs bénéficiaires |
| `Transfer` → `Beneficiary` | N..1 | Un transfert cible un bénéficiaire |
| `Transfer` → `Agent` | N..1 | Agent créateur du transfert |
| `Transfer` → `Agency` | N..1 source et destination | Agences d’envoi et de retrait |
| `Transfer` → `TransferPayment` | 1..0..1 | Paiement unique pour un transfert standard |
| `Transfer` → `Commission` | 1..N | Commission centrale et agence |
| `CashRegister` → `CashMovement` | 1..N | Mouvements financiers d’une caisse |
| `Transfer` → `AmlAlert` | 1..N | Alertes générées lors des contrôles AML |
| `User` → `KycDocument` | 1..N | Documents KYC de l’utilisateur |
| `User` → `Notification` | 1..N | Notifications destinées à l’utilisateur |

---

## 8. Entités JPA principales

### 8.1 User, Admin, Manager, Agent, Client

Stratégie recommandée : héritage JPA `JOINED`.

#### `User`

Attributs principaux :

- `id: Long`
- `uuid: String`
- `email: String`
- `passwordHash: String`
- `firstName: String`
- `lastName: String`
- `phoneNumber: String`
- `role: Role`
- `status: UserStatus`
- `twoFactorEnabled: boolean`
- `preferredLanguage: Language`
- `createdAt: LocalDateTime`
- `updatedAt: LocalDateTime`
- `lastLoginAt: LocalDateTime`

#### `Admin`

- `department: String`
- `superAdmin: boolean`

#### `Manager`

- `agency: Agency`
- `approvalLimit: BigDecimal`

#### `Agent`

- `agency: Agency`
- `employeeCode: String`
- `cashRegister: CashRegister`

#### `Client`

- `identityType: IdentityType`
- `identityNumberEncrypted: String`
- `dateOfBirth: LocalDate`
- `country: Country`
- `kycStatus: KycStatus`

### 8.2 Sécurité

#### `RefreshToken`

- `id: Long`
- `tokenHash: String`
- `user: User`
- `expiresAt: LocalDateTime`
- `revoked: boolean`
- `createdAt: LocalDateTime`
- `revokedAt: LocalDateTime`
- `ipAddress: String`
- `userAgent: String`

#### `OtpVerification`

- `id: Long`
- `user: User`
- `otpHash: String`
- `purpose: OtpPurpose`
- `channel: NotificationChannel`
- `expiresAt: LocalDateTime`
- `verified: boolean`
- `attemptCount: int`
- `createdAt: LocalDateTime`

#### `AuditLog`

- `id: Long`
- `actorUserId: Long`
- `actorEmail: String`
- `action: AuditAction`
- `entityType: String`
- `entityId: String`
- `ipAddress: String`
- `userAgent: String`
- `detailsJson: String`
- `createdAt: LocalDateTime`

### 8.3 Référentiel

#### `Country`

- `id: Long`
- `isoCode: String`
- `name: String`
- `phonePrefix: String`
- `currency: Currency`
- `active: boolean`

#### `Currency`

- `id: Long`
- `code: String`
- `name: String`
- `symbol: String`
- `scale: int`
- `active: boolean`

#### `ExchangeRate`

- `id: Long`
- `sourceCurrency: Currency`
- `targetCurrency: Currency`
- `rate: BigDecimal`
- `source: RateSource`
- `validFrom: LocalDateTime`
- `active: boolean`

#### `ExchangeRateHistory`

- `id: Long`
- `sourceCurrencyCode: String`
- `targetCurrencyCode: String`
- `oldRate: BigDecimal`
- `newRate: BigDecimal`
- `source: RateSource`
- `changedBy: User`
- `changedAt: LocalDateTime`

### 8.4 Agences, corridors et frais

#### `Agency`

- `id: Long`
- `code: String`
- `name: String`
- `address: String`
- `city: String`
- `country: Country`
- `dailyLimit: BigDecimal`
- `status: AgencyStatus`
- `createdAt: LocalDateTime`

#### `Corridor`

- `id: Long`
- `sourceCountry: Country`
- `destinationCountry: Country`
- `active: boolean`
- `dailyLimit: BigDecimal`
- `monthlyLimit: BigDecimal`

#### `FeeGrid`

- `id: Long`
- `corridor: Corridor`
- `sourceCurrency: Currency`
- `targetCurrency: Currency`
- `minAmount: BigDecimal`
- `maxAmount: BigDecimal`
- `fixedFee: BigDecimal`
- `percentageFee: BigDecimal`
- `agencyCommissionRate: BigDecimal`
- `centralCommissionRate: BigDecimal`
- `validFrom: LocalDate`
- `validTo: LocalDate`
- `active: boolean`

### 8.5 Transferts et finance

#### `Beneficiary`

- `id: Long`
- `client: Client`
- `firstName: String`
- `lastName: String`
- `phoneNumber: String`
- `country: Country`
- `identityType: IdentityType`
- `identityNumberEncrypted: String`
- `createdAt: LocalDateTime`

#### `Transfer`

- `id: Long`
- `reference: String`
- `withdrawalCodeHash: String`
- `sender: Client`
- `beneficiary: Beneficiary`
- `sourceAgency: Agency`
- `destinationAgency: Agency`
- `createdByAgent: Agent`
- `sourceCountry: Country`
- `destinationCountry: Country`
- `sourceCurrency: Currency`
- `targetCurrency: Currency`
- `sentAmount: BigDecimal`
- `feeAmount: BigDecimal`
- `exchangeRateApplied: BigDecimal`
- `receivedAmount: BigDecimal`
- `status: TransferStatus`
- `channel: TransferChannel`
- `expiresAt: LocalDateTime`
- `createdAt: LocalDateTime`
- `paidAt: LocalDateTime`
- `cancelledAt: LocalDateTime`

#### `TransferPayment`

- `id: Long`
- `transfer: Transfer`
- `paidByAgent: Agent`
- `paidAtAgency: Agency`
- `beneficiaryIdentityType: IdentityType`
- `beneficiaryIdentityNumberEncrypted: String`
- `paidAmount: BigDecimal`
- `paidAt: LocalDateTime`

#### `CashRegister`

- `id: Long`
- `agency: Agency`
- `agent: Agent`
- `currency: Currency`
- `openingBalance: BigDecimal`
- `currentBalance: BigDecimal`
- `status: CashRegisterStatus`
- `openedAt: LocalDateTime`
- `closedAt: LocalDateTime`

#### `CashMovement`

- `id: Long`
- `cashRegister: CashRegister`
- `type: CashMovementType`
- `amount: BigDecimal`
- `currency: Currency`
- `transfer: Transfer`
- `reason: String`
- `createdBy: User`
- `createdAt: LocalDateTime`

#### `Commission`

- `id: Long`
- `transfer: Transfer`
- `agency: Agency`
- `agencyPart: BigDecimal`
- `centralPart: BigDecimal`
- `currency: Currency`
- `createdAt: LocalDateTime`

### 8.6 KYC / AML / Notifications / Mobile Money

#### `KycDocument`

- `id: Long`
- `user: User`
- `documentType: KycDocumentType`
- `documentNumberEncrypted: String`
- `filePath: String`
- `status: KycStatus`
- `rejectionReason: String`
- `uploadedAt: LocalDateTime`
- `reviewedBy: User`
- `reviewedAt: LocalDateTime`

#### `AmlAlert`

- `id: Long`
- `transfer: Transfer`
- `user: User`
- `type: AmlAlertType`
- `riskLevel: RiskLevel`
- `status: AmlAlertStatus`
- `description: String`
- `createdAt: LocalDateTime`
- `reviewedBy: User`
- `reviewedAt: LocalDateTime`

#### `WatchlistEntry`

- `id: Long`
- `firstName: String`
- `lastName: String`
- `country: Country`
- `source: String`
- `active: boolean`
- `createdAt: LocalDateTime`

#### `Notification`

- `id: Long`
- `recipient: User`
- `channel: NotificationChannel`
- `title: String`
- `message: String`
- `status: NotificationStatus`
- `relatedEntityType: String`
- `relatedEntityId: String`
- `createdAt: LocalDateTime`
- `sentAt: LocalDateTime`

#### `MobileMoneyTransfer`

- `id: Long`
- `transfer: Transfer`
- `operator: MobileMoneyOperator`
- `walletPhoneNumber: String`
- `operatorTransactionReference: String`
- `status: MobileMoneyStatus`
- `reconciliationStatus: ReconciliationStatus`
- `createdAt: LocalDateTime`
- `reconciledAt: LocalDateTime`

---

## 9. Énumérations Java nécessaires

```java
public enum Role { ROLE_ADMIN, ROLE_MANAGER, ROLE_AGENT, ROLE_CLIENT }
public enum UserStatus { ACTIVE, SUSPENDED, DISABLED, PENDING_VERIFICATION }
public enum Language { FR, EN, AR }
public enum IdentityType { CIN, PASSPORT, RESIDENCE_CARD }
public enum KycStatus { NOT_SUBMITTED, PENDING, APPROVED, REJECTED, EXPIRED }
public enum OtpPurpose { LOGIN_2FA, TRANSFER_VALIDATION, PAYOUT_VALIDATION, PASSWORD_RESET }
public enum NotificationChannel { EMAIL, SMS, PUSH }
public enum NotificationStatus { PENDING, SENT, FAILED, READ }
public enum RateSource { MANUAL, EXTERNAL_API, SYSTEM }
public enum AgencyStatus { ACTIVE, SUSPENDED, CLOSED }
public enum TransferStatus { DRAFT, PENDING_PAYMENT, AVAILABLE, PAID, CANCELLED, EXPIRED, BLOCKED_AML }
public enum TransferChannel { AGENCY, ONLINE, MOBILE_MONEY }
public enum CashRegisterStatus { OPEN, CLOSED, BLOCKED }
public enum CashMovementType { CASH_IN, CASH_OUT, TRANSFER_SEND, TRANSFER_PAYOUT, ADJUSTMENT, CLOSING_DIFFERENCE }
public enum AmlAlertType { THRESHOLD_EXCEEDED, WATCHLIST_MATCH, REPEATED_ATTEMPTS, SUSPICIOUS_CORRIDOR }
public enum RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }
public enum AmlAlertStatus { OPEN, UNDER_REVIEW, RESOLVED, FALSE_POSITIVE }
public enum KycDocumentType { CIN_FRONT, CIN_BACK, PASSPORT, PROOF_OF_ADDRESS }
public enum MobileMoneyOperator { ORANGE_MONEY, WAVE, MPESA }
public enum MobileMoneyStatus { PENDING, SENT_TO_OPERATOR, CONFIRMED, FAILED, CANCELLED }
public enum ReconciliationStatus { NOT_RECONCILED, RECONCILED, MISMATCH }
public enum AuditAction { LOGIN, LOGOUT, CREATE_TRANSFER, PAY_TRANSFER, CANCEL_TRANSFER, UPDATE_RATE, UPDATE_FEE_GRID, KYC_REVIEW, AML_REVIEW, CASH_CLOSING, USER_CREATED, USER_SUSPENDED }
```

---

## 10. Services backend à créer

| Service | Responsabilités |
|---|---|
| `AuthService` | Login, vérification mot de passe, émission JWT, déclenchement 2FA |
| `RefreshTokenService` | Création, rotation, révocation, vérification expiration |
| `TwoFactorService` | Génération OTP, hash OTP, vérification, limitation des tentatives |
| `UserService` | Gestion utilisateurs, activation, suspension, profils |
| `CountryService` | CRUD pays, activation/désactivation |
| `CurrencyService` | CRUD devises |
| `ExchangeRateService` | Taux actifs, historique, conversion |
| `AgencyService` | Agences, statut, affectation managers/agents |
| `CorridorService` | Activation corridors, limites, vérification disponibilité |
| `FeeGridService` | CRUD grilles, simulation frais |
| `FeeCalculationService` | Calcul frais, commission agence, commission centrale |
| `BeneficiaryService` | Bénéficiaires clients, vérification identité |
| `TransferService` | Création, validation, recherche, annulation, expiration |
| `PayoutService` | Paiement/retrait, contrôle code de retrait, contrôle identité |
| `CashRegisterService` | Ouverture, solde, clôture, écarts |
| `CashMovementService` | Enregistrement mouvements de caisse |
| `CommissionService` | Calcul et consultation commissions |
| `KycService` | Upload, contrôle, validation/rejet documents |
| `AmlService` | Contrôle seuils, watchlist fictive, génération alertes |
| `WatchlistService` | CRUD liste de surveillance |
| `NotificationService` | Création/envoi simulé email/SMS/push |
| `MobileMoneyService` | Simulation opérateur, statut, réconciliation |
| `DashboardService` | Statistiques admin, manager, agent, client |
| `AuditService` | Journalisation des actions sensibles |
| `ChatbotService` | FAQ, suivi statut, estimation frais, escalade simulée |

---

## 11. Repositories JPA à créer

| Repository | Méthodes utiles |
|---|---|
| `UserRepository` | `findByEmail`, `existsByEmail`, `findByRole`, `findByStatus` |
| `AdminRepository` | `findBySuperAdminTrue` |
| `ManagerRepository` | `findByAgencyId` |
| `AgentRepository` | `findByAgencyId`, `findByEmployeeCode` |
| `ClientRepository` | `findByEmail`, `findByPhoneNumber` |
| `RefreshTokenRepository` | `findByTokenHash`, `findByUserIdAndRevokedFalse` |
| `OtpVerificationRepository` | `findTopByUserIdAndPurposeOrderByCreatedAtDesc` |
| `AuditLogRepository` | `findByActorUserId`, `findByAction`, `findByCreatedAtBetween` |
| `CountryRepository` | `findByIsoCode`, `findByActiveTrue` |
| `CurrencyRepository` | `findByCode`, `findByActiveTrue` |
| `ExchangeRateRepository` | `findActiveRate(source, target)` |
| `ExchangeRateHistoryRepository` | `findBySourceCurrencyCodeAndTargetCurrencyCodeOrderByChangedAtDesc` |
| `AgencyRepository` | `findByCode`, `findByCountryId`, `findByStatus` |
| `CorridorRepository` | `findBySourceCountryIdAndDestinationCountryId`, `findByActiveTrue` |
| `FeeGridRepository` | `findActiveGrid(corridor, amount, date)` |
| `BeneficiaryRepository` | `findByClientId`, `findByPhoneNumber` |
| `TransferRepository` | `findByReference`, `findByStatus`, `findBySenderId`, `findByBeneficiaryPhone`, `findByCreatedAtBetween` |
| `TransferPaymentRepository` | `findByTransferId` |
| `CashRegisterRepository` | `findByAgentIdAndStatus`, `findByAgencyIdAndStatus` |
| `CashMovementRepository` | `findByCashRegisterId`, `findByCreatedAtBetween` |
| `CommissionRepository` | `findByTransferId`, `findByAgencyId` |
| `AgencyReportRepository` | `findByAgencyIdAndPeriod` |
| `KycDocumentRepository` | `findByUserId`, `findByStatus` |
| `AmlAlertRepository` | `findByStatus`, `findByRiskLevel`, `findByTransferId` |
| `WatchlistEntryRepository` | `findByLastNameIgnoreCaseAndActiveTrue` |
| `NotificationRepository` | `findByRecipientId`, `findByStatus` |
| `MobileMoneyTransferRepository` | `findByTransferReference`, `findByReconciliationStatus` |

---

## 12. Contrôleurs REST à créer

| Controller | Base URL | Modules couverts |
|---|---|---|
| `AuthController` | `/api/v1/auth` | Login, register, 2FA, refresh, logout |
| `UserController` | `/api/v1/users` | Utilisateurs, profils, rôles |
| `CountryController` | `/api/v1/countries` | Pays |
| `CurrencyController` | `/api/v1/currencies` | Devises |
| `ExchangeRateController` | `/api/v1/exchange-rates` | Taux et conversion |
| `AgencyController` | `/api/v1/agencies` | Agences et affectations |
| `CorridorController` | `/api/v1/corridors` | Corridors |
| `FeeGridController` | `/api/v1/fee-grids` | Grilles de frais et simulation |
| `BeneficiaryController` | `/api/v1/beneficiaries` | Bénéficiaires |
| `TransferController` | `/api/v1/transfers` | Création, validation, suivi, annulation |
| `PayoutController` | `/api/v1/payouts` | Paiement/retrait |
| `CashRegisterController` | `/api/v1/cash-registers` | Caisse |
| `CommissionController` | `/api/v1/commissions` | Commissions |
| `KycController` | `/api/v1/kyc` | Documents KYC |
| `AmlController` | `/api/v1/aml` | AML et watchlist |
| `NotificationController` | `/api/v1/notifications` | Notifications |
| `MobileMoneyController` | `/api/v1/mobile-money` | Mobile Money simulé |
| `DashboardController` | `/api/v1/dashboards` | Dashboards |
| `ReportController` | `/api/v1/reports` | Rapports PDF/CSV/JSON |
| `AuditController` | `/api/v1/audit-logs` | Journal d’audit |
| `ChatbotController` | `/api/v1/chatbot` | Assistant intelligent |

---

## 13. DTOs request/response à prévoir

### 13.1 DTOs communs

```java
ApiResponse<T> { boolean success; String message; T data; LocalDateTime timestamp; }
PageResponse<T> { List<T> content; int page; int size; long totalElements; int totalPages; }
ErrorResponse { String code; String message; Map<String, String> fieldErrors; LocalDateTime timestamp; }
```

### 13.2 Authentification

```java
LoginRequest { String email; String password; }
LoginResponse { boolean twoFactorRequired; String temporaryToken; JwtResponse tokens; UserSummaryResponse user; }
OtpVerifyRequest { String temporaryToken; String otpCode; OtpPurpose purpose; }
JwtResponse { String accessToken; String refreshToken; String tokenType; long expiresIn; }
RefreshTokenRequest { String refreshToken; }
LogoutRequest { String refreshToken; }
ClientRegisterRequest { String firstName; String lastName; String email; String phoneNumber; String password; Long countryId; }
```

### 13.3 Utilisateurs

```java
UserCreateRequest { String firstName; String lastName; String email; String phoneNumber; Role role; Long agencyId; }
UserUpdateRequest { String firstName; String lastName; String phoneNumber; Language preferredLanguage; }
UserStatusUpdateRequest { UserStatus status; String reason; }
UserSummaryResponse { Long id; String email; String fullName; Role role; UserStatus status; String agencyName; }
UserProfileResponse { Long id; String firstName; String lastName; String email; String phoneNumber; Role role; Language preferredLanguage; }
```

### 13.4 Référentiel

```java
CountryRequest { String isoCode; String name; String phonePrefix; Long currencyId; boolean active; }
CountryResponse { Long id; String isoCode; String name; String phonePrefix; CurrencyResponse currency; boolean active; }
CurrencyRequest { String code; String name; String symbol; int scale; boolean active; }
CurrencyResponse { Long id; String code; String name; String symbol; int scale; boolean active; }
ExchangeRateRequest { Long sourceCurrencyId; Long targetCurrencyId; BigDecimal rate; RateSource source; }
ExchangeRateResponse { Long id; String sourceCurrency; String targetCurrency; BigDecimal rate; LocalDateTime validFrom; boolean active; }
ConversionRequest { String sourceCurrency; String targetCurrency; BigDecimal amount; }
ConversionResponse { BigDecimal sourceAmount; BigDecimal convertedAmount; BigDecimal rate; String sourceCurrency; String targetCurrency; }
```

### 13.5 Agences, corridors, frais

```java
AgencyRequest { String code; String name; String address; String city; Long countryId; BigDecimal dailyLimit; }
AgencyResponse { Long id; String code; String name; String city; String country; AgencyStatus status; BigDecimal dailyLimit; }
CorridorRequest { Long sourceCountryId; Long destinationCountryId; BigDecimal dailyLimit; BigDecimal monthlyLimit; boolean active; }
CorridorResponse { Long id; CountryResponse sourceCountry; CountryResponse destinationCountry; boolean active; BigDecimal dailyLimit; BigDecimal monthlyLimit; }
FeeGridRequest { Long corridorId; Long sourceCurrencyId; Long targetCurrencyId; BigDecimal minAmount; BigDecimal maxAmount; BigDecimal fixedFee; BigDecimal percentageFee; BigDecimal agencyCommissionRate; BigDecimal centralCommissionRate; LocalDate validFrom; LocalDate validTo; boolean active; }
FeeSimulationRequest { Long corridorId; String sourceCurrency; String targetCurrency; BigDecimal amount; }
FeeSimulationResponse { BigDecimal amount; BigDecimal feeAmount; BigDecimal totalToPay; BigDecimal exchangeRate; BigDecimal receivedAmount; BigDecimal agencyCommission; BigDecimal centralCommission; }
```

### 13.6 Transferts

```java
BeneficiaryRequest { String firstName; String lastName; String phoneNumber; Long countryId; IdentityType identityType; String identityNumber; }
BeneficiaryResponse { Long id; String fullName; String phoneNumber; String country; }
TransferCreateRequest { Long senderClientId; BeneficiaryRequest beneficiary; Long sourceAgencyId; Long destinationAgencyId; Long corridorId; String sourceCurrency; String targetCurrency; BigDecimal amount; TransferChannel channel; }
TransferResponse { Long id; String reference; TransferStatus status; String senderName; String beneficiaryName; BigDecimal sentAmount; BigDecimal feeAmount; BigDecimal receivedAmount; String sourceCurrency; String targetCurrency; BigDecimal exchangeRateApplied; LocalDateTime createdAt; LocalDateTime expiresAt; }
TransferConfirmRequest { String otpCode; }
TransferCancelRequest { String reason; }
TransferTrackingResponse { String reference; TransferStatus status; String sourceCountry; String destinationCountry; BigDecimal receivedAmount; LocalDateTime createdAt; LocalDateTime paidAt; }
```

### 13.7 Paiement, caisse, commissions

```java
PayoutSearchRequest { String withdrawalCode; String beneficiaryPhoneNumber; }
PayoutConfirmRequest { String transferReference; String withdrawalCode; IdentityType identityType; String identityNumber; String otpCode; }
PayoutResponse { String transferReference; TransferStatus status; BigDecimal paidAmount; String currency; LocalDateTime paidAt; }
CashRegisterOpenRequest { Long agentId; Long agencyId; String currencyCode; BigDecimal openingBalance; }
CashMovementRequest { CashMovementType type; BigDecimal amount; String currencyCode; String reason; Long transferId; }
CashClosingRequest { BigDecimal countedAmount; String comment; }
CashRegisterResponse { Long id; String agencyCode; String agentName; String currencyCode; BigDecimal openingBalance; BigDecimal currentBalance; CashRegisterStatus status; }
CommissionResponse { Long id; String transferReference; BigDecimal agencyPart; BigDecimal centralPart; String currency; }
```

### 13.8 KYC / AML / Notifications / Mobile Money / Dashboard

```java
KycReviewRequest { KycStatus status; String rejectionReason; }
KycDocumentResponse { Long id; KycDocumentType documentType; KycStatus status; LocalDateTime uploadedAt; String rejectionReason; }
WatchlistEntryRequest { String firstName; String lastName; Long countryId; String source; boolean active; }
AmlAlertResponse { Long id; String transferReference; AmlAlertType type; RiskLevel riskLevel; AmlAlertStatus status; String description; LocalDateTime createdAt; }
AmlReviewRequest { AmlAlertStatus status; String comment; }
NotificationResponse { Long id; NotificationChannel channel; String title; String message; NotificationStatus status; LocalDateTime createdAt; }
MobileMoneyRequest { String transferReference; MobileMoneyOperator operator; String walletPhoneNumber; }
MobileMoneyResponse { Long id; String transferReference; MobileMoneyOperator operator; MobileMoneyStatus status; ReconciliationStatus reconciliationStatus; String operatorTransactionReference; }
DashboardSummaryResponse { BigDecimal totalVolume; long transferCount; BigDecimal totalFees; BigDecimal totalCommissions; Map<String, Object> charts; }
ChatbotRequest { String message; String language; }
ChatbotResponse { String answer; boolean escalated; String intent; }
```

---

## 14. Contrat API REST complet

### 14.1 Conventions générales API

- Base URL : `/api/v1`
- Format : JSON UTF-8, sauf upload KYC et exports.
- Authentification : `Authorization: Bearer <accessToken>` pour tous les endpoints sécurisés.
- Pagination : `?page=0&size=20&sort=createdAt,desc`.
- Réponse standard :

```json
{
  "success": true,
  "message": "Operation completed",
  "data": {},
  "timestamp": "2026-05-24T20:00:00"
}
```

- Erreur standard :

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid request body",
  "fieldErrors": {
    "email": "must be a valid email"
  },
  "timestamp": "2026-05-24T20:00:00"
}
```

### 14.2 Codes HTTP communs

| Code | Signification |
|---:|---|
| 200 | Requête traitée avec succès |
| 201 | Ressource créée |
| 204 | Suppression ou action sans contenu |
| 400 | Données invalides |
| 401 | Non authentifié ou token invalide |
| 403 | Rôle insuffisant |
| 404 | Ressource introuvable |
| 409 | Conflit métier |
| 422 | Règle métier non respectée |
| 429 | Trop de tentatives |
| 500 | Erreur serveur |

---

### 14.3 Authentification, sécurité, refresh token, logout

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Auth | `POST /api/v1/auth/register-client` | Public | Inscription client self-service | Aucun | `ClientRegisterRequest` | `UserSummaryResponse` | 201, 400, 409 | Rate limit, BCrypt, audit `USER_CREATED` |
| Auth | `POST /api/v1/auth/login` | Public | Authentifie email/mot de passe. Si 2FA activé, retourne un `temporaryToken`. | Aucun | `LoginRequest` | `LoginResponse` | 200, 400, 401, 429 | Rate limit 5/10 min, BCrypt |
| Auth | `POST /api/v1/auth/verify-otp` | Public | Vérifie OTP 2FA et émet les tokens JWT. | Aucun | `OtpVerifyRequest` | `JwtResponse` + `UserSummaryResponse` | 200, 400, 401, 429 | OTP hashé, expiration, max tentatives |
| Auth | `POST /api/v1/auth/refresh` | Public | Renouvelle l’access token avec rotation du refresh token. | Aucun | `RefreshTokenRequest` | `JwtResponse` | 200, 400, 401 | Refresh token hashé et révocable |
| Auth | `POST /api/v1/auth/logout` | Authentifié | Révoque le refresh token courant. | Aucun | `LogoutRequest` | `{ "loggedOut": true }` | 200, 401 | JWT requis, audit `LOGOUT` |
| Auth | `POST /api/v1/auth/logout-all` | Authentifié | Révoque tous les refresh tokens de l’utilisateur. | Aucun | `{}` | `{ "revokedTokens": 3 }` | 200, 401 | JWT requis |
| Auth | `GET /api/v1/auth/me` | Authentifié | Retourne le profil connecté. | Aucun | Aucun | `UserProfileResponse` | 200, 401 | JWT requis |

Exemple `LoginRequest` :

```json
{
  "email": "client@example.com",
  "password": "Password@123"
}
```

Exemple `JwtResponse` :

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "b1d4d2d5-...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

### 14.4 Gestion OTP / 2FA

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| OTP | `POST /api/v1/otp/request` | Authentifié ou temporary token | Demande un OTP pour login ou opération sensible. | Aucun | `{ "purpose": "TRANSFER_VALIDATION", "channel": "SMS" }` | `{ "otpId": 12, "expiresInSeconds": 300 }` | 200, 400, 401, 429 | Rate limit, OTP non stocké en clair |
| OTP | `POST /api/v1/otp/verify` | Authentifié ou temporary token | Vérifie un OTP. | Aucun | `OtpVerifyRequest` | `{ "verified": true }` | 200, 400, 401, 429 | Max tentatives, expiration |
| OTP | `POST /api/v1/otp/enable-2fa` | Authentifié | Active la 2FA sur le compte. | Aucun | `{ "otpCode": "123456" }` | `{ "twoFactorEnabled": true }` | 200, 400, 401 | JWT + OTP |
| OTP | `POST /api/v1/otp/disable-2fa` | Authentifié | Désactive la 2FA. | Aucun | `{ "password": "Password@123", "otpCode": "123456" }` | `{ "twoFactorEnabled": false }` | 200, 400, 401 | JWT + mot de passe + OTP |

---

### 14.5 Utilisateurs, rôles et profils

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Users | `GET /api/v1/users` | ADMIN, MANAGER | Liste paginée des utilisateurs. MANAGER limité à son agence. | Query: `role`, `status`, `agencyId`, `page`, `size` | Aucun | `PageResponse<UserSummaryResponse>` | 200, 401, 403 | Filtrage par rôle |
| Users | `POST /api/v1/users` | ADMIN, MANAGER | Crée un utilisateur interne. MANAGER peut créer uniquement des agents de son agence. | Aucun | `UserCreateRequest` | `UserSummaryResponse` | 201, 400, 403, 409 | BCrypt, audit |
| Users | `GET /api/v1/users/{id}` | ADMIN, MANAGER ou propriétaire | Détail utilisateur. | Path: `id` | Aucun | `UserProfileResponse` | 200, 401, 403, 404 | Contrôle propriétaire ou rôle |
| Users | `PUT /api/v1/users/{id}` | ADMIN ou propriétaire | Met à jour le profil. | Path: `id` | `UserUpdateRequest` | `UserProfileResponse` | 200, 400, 403, 404 | Validation stricte |
| Users | `PATCH /api/v1/users/{id}/status` | ADMIN | Active/suspend/désactive un utilisateur. | Path: `id` | `UserStatusUpdateRequest` | `UserSummaryResponse` | 200, 400, 403, 404 | Audit `USER_SUSPENDED` |
| Users | `PATCH /api/v1/users/{id}/role` | ADMIN | Change le rôle applicatif. | Path: `id` | `{ "role": "ROLE_MANAGER" }` | `UserSummaryResponse` | 200, 400, 403, 404 | Interdit de retirer dernier super admin |
| Users | `GET /api/v1/users/{id}/audit-logs` | ADMIN | Journal d’audit d’un utilisateur. | Path: `id`; Query: dates | `AuditLogResponse[]` | 200, 403, 404 | Accès admin uniquement |

---

### 14.6 Pays, devises et taux de change

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Pays | `GET /api/v1/countries` | Authentifié | Liste des pays. | Query: `active` | Aucun | `CountryResponse[]` | 200, 401 | JWT requis |
| Pays | `POST /api/v1/countries` | ADMIN | Crée un pays. | Aucun | `CountryRequest` | `CountryResponse` | 201, 400, 403, 409 | Audit |
| Pays | `GET /api/v1/countries/{id}` | Authentifié | Détail pays. | Path: `id` | Aucun | `CountryResponse` | 200, 404 | JWT requis |
| Pays | `PUT /api/v1/countries/{id}` | ADMIN | Met à jour un pays. | Path: `id` | `CountryRequest` | `CountryResponse` | 200, 400, 403, 404 | Audit |
| Pays | `PATCH /api/v1/countries/{id}/activation` | ADMIN | Active/désactive un pays. | Path: `id` | `{ "active": true }` | `CountryResponse` | 200, 400, 403, 404 | Audit |
| Devises | `GET /api/v1/currencies` | Authentifié | Liste des devises. | Query: `active` | Aucun | `CurrencyResponse[]` | 200, 401 | JWT requis |
| Devises | `POST /api/v1/currencies` | ADMIN | Crée une devise. | Aucun | `CurrencyRequest` | `CurrencyResponse` | 201, 400, 403, 409 | Audit |
| Devises | `PUT /api/v1/currencies/{id}` | ADMIN | Met à jour une devise. | Path: `id` | `CurrencyRequest` | `CurrencyResponse` | 200, 400, 403, 404 | Audit |
| Taux | `GET /api/v1/exchange-rates` | ADMIN, MANAGER, AGENT | Liste des taux actifs. | Query: `source`, `target`, `active` | Aucun | `ExchangeRateResponse[]` | 200, 401 | JWT requis |
| Taux | `POST /api/v1/exchange-rates` | ADMIN | Crée/met à jour un taux actif. | Aucun | `ExchangeRateRequest` | `ExchangeRateResponse` | 201, 400, 403 | Historisation obligatoire |
| Taux | `POST /api/v1/exchange-rates/convert` | ADMIN, MANAGER, AGENT, CLIENT | Convertit un montant. | Aucun | `ConversionRequest` | `ConversionResponse` | 200, 400, 404 | Taux actif requis |
| Taux | `GET /api/v1/exchange-rates/history` | ADMIN, MANAGER | Historique des taux. | Query: `source`, `target`, `from`, `to` | Aucun | `ExchangeRateHistoryResponse[]` | 200, 403 | Audit consultable |

---

### 14.7 Agences et corridors

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Agences | `GET /api/v1/agencies` | ADMIN, MANAGER, AGENT | Liste des agences. | Query: `countryId`, `status`, `page`, `size` | Aucun | `PageResponse<AgencyResponse>` | 200, 401 | JWT requis |
| Agences | `POST /api/v1/agencies` | ADMIN | Crée une agence. | Aucun | `AgencyRequest` | `AgencyResponse` | 201, 400, 403, 409 | Audit |
| Agences | `GET /api/v1/agencies/{id}` | ADMIN, MANAGER, AGENT | Détail agence. | Path: `id` | Aucun | `AgencyResponse` | 200, 404 | JWT requis |
| Agences | `PUT /api/v1/agencies/{id}` | ADMIN | Met à jour une agence. | Path: `id` | `AgencyRequest` | `AgencyResponse` | 200, 400, 403, 404 | Audit |
| Agences | `PATCH /api/v1/agencies/{id}/status` | ADMIN | Active/suspend/ferme une agence. | Path: `id` | `{ "status": "SUSPENDED" }` | `AgencyResponse` | 200, 400, 403, 404 | Audit |
| Agences | `POST /api/v1/agencies/{id}/agents/{agentId}` | ADMIN, MANAGER | Affecte un agent à l’agence. | Path: `id`, `agentId` | `{}` | `AgencyResponse` | 200, 400, 403, 404 | MANAGER limité à son agence |
| Agences | `POST /api/v1/agencies/{id}/managers/{managerId}` | ADMIN | Affecte un manager à une agence. | Path: `id`, `managerId` | `{}` | `AgencyResponse` | 200, 400, 403, 404 | Admin uniquement |
| Corridors | `GET /api/v1/corridors` | Authentifié | Liste des corridors. | Query: `sourceCountryId`, `destinationCountryId`, `active` | Aucun | `CorridorResponse[]` | 200, 401 | JWT requis |
| Corridors | `POST /api/v1/corridors` | ADMIN | Crée un corridor. | Aucun | `CorridorRequest` | `CorridorResponse` | 201, 400, 403, 409 | Audit |
| Corridors | `PUT /api/v1/corridors/{id}` | ADMIN | Met à jour limites et statut. | Path: `id` | `CorridorRequest` | `CorridorResponse` | 200, 400, 403, 404 | Audit |
| Corridors | `PATCH /api/v1/corridors/{id}/activation` | ADMIN | Active/désactive un corridor. | Path: `id` | `{ "active": false }` | `CorridorResponse` | 200, 400, 403, 404 | Impact création transfert |

---

### 14.8 Grilles de frais

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Frais | `GET /api/v1/fee-grids` | ADMIN, MANAGER, AGENT | Liste les grilles. | Query: `corridorId`, `active`, `page`, `size` | Aucun | `PageResponse<FeeGridResponse>` | 200, 401 | JWT requis |
| Frais | `POST /api/v1/fee-grids` | ADMIN | Crée une grille. | Aucun | `FeeGridRequest` | `FeeGridResponse` | 201, 400, 403, 409 | Interdire chevauchement tranches actives |
| Frais | `PUT /api/v1/fee-grids/{id}` | ADMIN | Met à jour une grille. | Path: `id` | `FeeGridRequest` | `FeeGridResponse` | 200, 400, 403, 404 | Audit |
| Frais | `PATCH /api/v1/fee-grids/{id}/activation` | ADMIN | Active/désactive une grille. | Path: `id` | `{ "active": true }` | `FeeGridResponse` | 200, 400, 403, 404 | Audit |
| Frais | `POST /api/v1/fee-grids/simulate` | ADMIN, MANAGER, AGENT, CLIENT | Simule frais, taux et montant reçu. | Aucun | `FeeSimulationRequest` | `FeeSimulationResponse` | 200, 400, 404 | Corridor, taux et grille actifs requis |
| Frais | `GET /api/v1/fee-grids/export` | ADMIN | Export PDF/CSV. | Query: `format=PDF|CSV`, `corridorId` | Aucun | Fichier | 200, 400, 403 | Admin uniquement |

---

### 14.9 Bénéficiaires

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Bénéficiaires | `GET /api/v1/beneficiaries` | CLIENT, AGENT | Liste des bénéficiaires. CLIENT limité aux siens. | Query: `clientId`, `page`, `size` | Aucun | `PageResponse<BeneficiaryResponse>` | 200, 401, 403 | Contrôle propriétaire |
| Bénéficiaires | `POST /api/v1/beneficiaries` | CLIENT, AGENT | Crée un bénéficiaire. | Aucun | `BeneficiaryRequest` | `BeneficiaryResponse` | 201, 400, 401 | Chiffrement identité |
| Bénéficiaires | `GET /api/v1/beneficiaries/{id}` | CLIENT, AGENT | Détail bénéficiaire. | Path: `id` | Aucun | `BeneficiaryResponse` | 200, 403, 404 | Propriétaire ou agent |
| Bénéficiaires | `PUT /api/v1/beneficiaries/{id}` | CLIENT, AGENT | Met à jour un bénéficiaire. | Path: `id` | `BeneficiaryRequest` | `BeneficiaryResponse` | 200, 400, 403, 404 | Audit si identité modifiée |
| Bénéficiaires | `DELETE /api/v1/beneficiaries/{id}` | CLIENT | Supprime/désactive un bénéficiaire. | Path: `id` | Aucun | Aucun | 204, 403, 404 | Suppression logique recommandée |

---

### 14.10 Transferts d’argent

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Transferts | `POST /api/v1/transfers/simulate` | AGENT, CLIENT | Simule un transfert avant création. | Aucun | `TransferCreateRequest` sans bénéficiaire complet possible | `FeeSimulationResponse` | 200, 400, 404 | KYC/corridor/frais/taux vérifiés |
| Transferts | `POST /api/v1/transfers` | AGENT | Crée un transfert en état `PENDING_PAYMENT` ou `AVAILABLE` selon validation paiement. | Aucun | `TransferCreateRequest` | `TransferResponse` | 201, 400, 403, 409, 422 | Agent agence active, KYC, AML, audit |
| Transferts | `POST /api/v1/transfers/{reference}/confirm-payment` | AGENT | Confirme le paiement à l’envoi, génère code retrait. | Path: `reference` | `TransferConfirmRequest` | `TransferResponse` | 200, 400, 403, 404, 422 | OTP opération sensible, mouvement caisse CASH_IN |
| Transferts | `GET /api/v1/transfers` | ADMIN, MANAGER, AGENT, CLIENT | Recherche paginée des transferts. | Query: `status`, `reference`, `clientId`, `agencyId`, `from`, `to`, `page`, `size` | Aucun | `PageResponse<TransferResponse>` | 200, 401, 403 | Filtrage par rôle |
| Transferts | `GET /api/v1/transfers/{reference}` | ADMIN, MANAGER, AGENT, CLIENT | Détail transfert. | Path: `reference` | Aucun | `TransferResponse` | 200, 403, 404 | Client limité aux siens |
| Transferts | `GET /api/v1/transfers/track/{reference}` | CLIENT, AGENT | Suivi par référence. | Path: `reference` | Aucun | `TransferTrackingResponse` | 200, 404 | Pas de données sensibles en tracking |
| Transferts | `PATCH /api/v1/transfers/{reference}/cancel` | ADMIN, MANAGER, AGENT | Annule un transfert non payé. | Path: `reference` | `TransferCancelRequest` | `TransferResponse` | 200, 400, 403, 404, 422 | Remboursement caisse si nécessaire, audit |
| Transferts | `PATCH /api/v1/transfers/{reference}/expire` | ADMIN | Force l’expiration d’un transfert. | Path: `reference` | `{ "reason": "Expired by admin" }` | `TransferResponse` | 200, 403, 404, 422 | Audit |
| Transferts | `GET /api/v1/transfers/{reference}/receipt` | AGENT, CLIENT | Génère reçu PDF. | Path: `reference`; Query: `type=SEND|PAYOUT` | Aucun | Fichier PDF | 200, 403, 404 | Client limité aux siens |

Exemple `TransferCreateRequest` :

```json
{
  "senderClientId": 45,
  "beneficiary": {
    "firstName": "Ali",
    "lastName": "Diop",
    "phoneNumber": "+221770000000",
    "countryId": 2,
    "identityType": "PASSPORT",
    "identityNumber": "P1234567"
  },
  "sourceAgencyId": 1,
  "destinationAgencyId": 5,
  "corridorId": 3,
  "sourceCurrency": "MAD",
  "targetCurrency": "XOF",
  "amount": 1500.00,
  "channel": "AGENCY"
}
```

Exemple `TransferResponse` :

```json
{
  "id": 1001,
  "reference": "OKT-20260524-8F3A2C",
  "status": "AVAILABLE",
  "senderName": "Sara Benali",
  "beneficiaryName": "Ali Diop",
  "sentAmount": 1500.00,
  "feeAmount": 45.00,
  "receivedAmount": 91500.00,
  "sourceCurrency": "MAD",
  "targetCurrency": "XOF",
  "exchangeRateApplied": 61.00,
  "createdAt": "2026-05-24T20:10:00",
  "expiresAt": "2026-06-23T20:10:00"
}
```

---

### 14.11 Paiement et retrait de transfert

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Payout | `POST /api/v1/payouts/search` | AGENT | Recherche un transfert payable par code ou téléphone. | Aucun | `PayoutSearchRequest` | `TransferTrackingResponse` | 200, 400, 403, 404 | Masquer détails sensibles |
| Payout | `POST /api/v1/payouts/validate` | AGENT | Valide code retrait, statut et identité avant paiement. | Aucun | `{ "transferReference": "OKT-...", "withdrawalCode": "A8F3K2L9", "identityType": "PASSPORT", "identityNumber": "P1234567" }` | `{ "valid": true, "requiresOtp": true }` | 200, 400, 403, 404, 422 | Code hashé, identité chiffrée |
| Payout | `POST /api/v1/payouts/confirm` | AGENT | Confirme le paiement au bénéficiaire. | Aucun | `PayoutConfirmRequest` | `PayoutResponse` | 200, 400, 403, 404, 422 | OTP, caisse suffisante, audit, CASH_OUT |
| Payout | `GET /api/v1/payouts/{transferReference}/receipt` | AGENT | Reçu de paiement PDF. | Path: `transferReference` | Aucun | Fichier PDF | 200, 403, 404 | Agent uniquement |

Règles clés :

- Le transfert doit être `AVAILABLE`.
- Le code de retrait doit correspondre au hash stocké.
- Le transfert ne doit pas être expiré.
- La caisse de l’agent doit avoir un solde suffisant dans la devise cible.
- Le paiement met le transfert à `PAID` et crée un `TransferPayment`.

---

### 14.12 Caisse et mouvements de caisse

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Caisse | `POST /api/v1/cash-registers/open` | AGENT, MANAGER | Ouvre une caisse pour un agent. | Aucun | `CashRegisterOpenRequest` | `CashRegisterResponse` | 201, 400, 403, 409 | Une seule caisse ouverte par agent/devise |
| Caisse | `GET /api/v1/cash-registers/current` | AGENT | Caisse ouverte de l’agent connecté. | Query: `currencyCode` | Aucun | `CashRegisterResponse` | 200, 404 | Agent connecté |
| Caisse | `GET /api/v1/cash-registers/{id}/movements` | AGENT, MANAGER, ADMIN | Mouvements de caisse. | Path: `id`; Query: dates, page, size | Aucun | `PageResponse<CashMovementResponse>` | 200, 403, 404 | MANAGER limité à son agence |
| Caisse | `POST /api/v1/cash-registers/{id}/movements` | AGENT, MANAGER | Ajoute un mouvement manuel. | Path: `id` | `CashMovementRequest` | `CashMovementResponse` | 201, 400, 403, 404 | Audit, justification obligatoire |
| Caisse | `POST /api/v1/cash-registers/{id}/close` | AGENT, MANAGER | Clôture et réconcilie la caisse. | Path: `id` | `CashClosingRequest` | `CashRegisterResponse` | 200, 400, 403, 404, 422 | Écart journalisé |
| Caisse | `GET /api/v1/cash-registers/agency/{agencyId}` | MANAGER, ADMIN | Caisses d’une agence. | Path: `agencyId`; Query: status, date | Aucun | `CashRegisterResponse[]` | 200, 403, 404 | Scope agence |

---

### 14.13 Commissions

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Commissions | `GET /api/v1/commissions` | ADMIN, MANAGER | Liste des commissions. | Query: `agencyId`, `from`, `to`, `page`, `size` | Aucun | `PageResponse<CommissionResponse>` | 200, 403 | MANAGER limité à son agence |
| Commissions | `GET /api/v1/commissions/transfer/{reference}` | ADMIN, MANAGER | Commissions d’un transfert. | Path: `reference` | Aucun | `CommissionResponse[]` | 200, 403, 404 | Scope agence |
| Commissions | `GET /api/v1/commissions/summary` | ADMIN, MANAGER | Synthèse commissions. | Query: `agencyId`, `period=DAILY|MONTHLY`, dates | Aucun | `{ "agencyPart": 1000, "centralPart": 500 }` | 200, 403 | Scope agence |

---

### 14.14 KYC

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| KYC | `POST /api/v1/kyc/documents` | CLIENT, AGENT | Upload document KYC. | Aucun | `multipart/form-data`: `file`, `documentType`, `documentNumber` | `KycDocumentResponse` | 201, 400, 401, 413 | Chiffrement numéro, contrôle extension/taille |
| KYC | `GET /api/v1/kyc/documents/me` | CLIENT | Documents du client connecté. | Aucun | Aucun | `KycDocumentResponse[]` | 200, 401 | Propriétaire uniquement |
| KYC | `GET /api/v1/kyc/users/{userId}/documents` | ADMIN, MANAGER, AGENT | Documents d’un utilisateur. | Path: `userId` | Aucun | `KycDocumentResponse[]` | 200, 403, 404 | Scope selon rôle |
| KYC | `PATCH /api/v1/kyc/documents/{id}/review` | ADMIN, MANAGER | Valide ou rejette un document KYC. | Path: `id` | `KycReviewRequest` | `KycDocumentResponse` | 200, 400, 403, 404 | Audit `KYC_REVIEW` |
| KYC | `GET /api/v1/kyc/pending` | ADMIN, MANAGER | Documents en attente. | Query: page, size | Aucun | `PageResponse<KycDocumentResponse>` | 200, 403 | Scope agence si manager |

---

### 14.15 AML et listes de surveillance

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| AML | `GET /api/v1/aml/alerts` | ADMIN, MANAGER | Liste alertes AML. | Query: `status`, `riskLevel`, `page`, `size` | Aucun | `PageResponse<AmlAlertResponse>` | 200, 403 | Accès conformité |
| AML | `GET /api/v1/aml/alerts/{id}` | ADMIN, MANAGER | Détail alerte. | Path: `id` | Aucun | `AmlAlertResponse` | 200, 403, 404 | Scope manager |
| AML | `PATCH /api/v1/aml/alerts/{id}/review` | ADMIN, MANAGER | Traite une alerte. | Path: `id` | `AmlReviewRequest` | `AmlAlertResponse` | 200, 400, 403, 404 | Audit `AML_REVIEW` |
| AML | `POST /api/v1/aml/check-transfer` | AGENT, MANAGER, ADMIN | Contrôle AML avant validation. | Aucun | `{ "transferReference": "OKT-..." }` | `{ "blocked": false, "alerts": [] }` | 200, 400, 403, 404 | Peut bloquer transfert |
| Watchlist | `GET /api/v1/aml/watchlist` | ADMIN | Liste surveillance. | Query: `active`, page, size | Aucun | `PageResponse<WatchlistEntryResponse>` | 200, 403 | Admin uniquement |
| Watchlist | `POST /api/v1/aml/watchlist` | ADMIN | Ajoute une entrée fictive OFAC. | Aucun | `WatchlistEntryRequest` | `WatchlistEntryResponse` | 201, 400, 403 | Audit |
| Watchlist | `PUT /api/v1/aml/watchlist/{id}` | ADMIN | Met à jour une entrée. | Path: `id` | `WatchlistEntryRequest` | `WatchlistEntryResponse` | 200, 400, 403, 404 | Audit |
| Watchlist | `DELETE /api/v1/aml/watchlist/{id}` | ADMIN | Désactive une entrée. | Path: `id` | Aucun | Aucun | 204, 403, 404 | Suppression logique |

---

### 14.16 Notifications

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Notifications | `GET /api/v1/notifications/me` | Authentifié | Notifications de l’utilisateur connecté. | Query: `status`, page, size | Aucun | `PageResponse<NotificationResponse>` | 200, 401 | Propriétaire uniquement |
| Notifications | `PATCH /api/v1/notifications/{id}/read` | Authentifié | Marque comme lue. | Path: `id` | `{}` | `NotificationResponse` | 200, 403, 404 | Propriétaire |
| Notifications | `POST /api/v1/notifications/test` | ADMIN | Envoi test simulé. | Aucun | `{ "channel": "EMAIL", "recipientUserId": 1, "message": "Test" }` | `NotificationResponse` | 201, 400, 403 | Admin uniquement |
| Notifications | `GET /api/v1/notifications/preferences` | CLIENT | Préférences de notification. | Aucun | Aucun | `{ "email": true, "sms": true, "push": false }` | 200, 401 | Client connecté |
| Notifications | `PUT /api/v1/notifications/preferences` | CLIENT | Met à jour préférences. | Aucun | `{ "email": true, "sms": false, "push": true }` | `{ "email": true, "sms": false, "push": true }` | 200, 400, 401 | Client connecté |

---

### 14.17 Mobile Money simulé

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Mobile Money | `POST /api/v1/mobile-money/transfers` | AGENT | Lance l’envoi vers opérateur simulé. | Aucun | `MobileMoneyRequest` | `MobileMoneyResponse` | 201, 400, 403, 404, 422 | Transfert disponible, opérateur supporté |
| Mobile Money | `GET /api/v1/mobile-money/transfers/{id}` | ADMIN, MANAGER, AGENT | Détail transfert Mobile Money. | Path: `id` | Aucun | `MobileMoneyResponse` | 200, 403, 404 | Scope agence |
| Mobile Money | `PATCH /api/v1/mobile-money/transfers/{id}/simulate-callback` | ADMIN | Simule callback opérateur. | Path: `id` | `{ "status": "CONFIRMED", "operatorTransactionReference": "OM-123" }` | `MobileMoneyResponse` | 200, 400, 403, 404 | Simulation uniquement |
| Mobile Money | `POST /api/v1/mobile-money/reconciliation` | ADMIN, MANAGER | Réconciliation avec relevé opérateur simulé. | Aucun | `{ "operator": "ORANGE_MONEY", "date": "2026-05-24" }` | `{ "reconciled": 10, "mismatches": 1 }` | 200, 400, 403 | Audit |
| Mobile Money | `GET /api/v1/mobile-money/dashboard` | ADMIN, MANAGER | Dashboard Mobile Money. | Query: dates, operator | Aucun | `DashboardSummaryResponse` | 200, 403 | Chart.js côté frontend |

---

### 14.18 Tableaux de bord et rapports

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Dashboards | `GET /api/v1/dashboards/admin` | ADMIN | Indicateurs consolidés. | Query: `from`, `to` | Aucun | `DashboardSummaryResponse` | 200, 403 | Admin uniquement |
| Dashboards | `GET /api/v1/dashboards/manager` | MANAGER | Indicateurs agence du manager. | Query: dates | Aucun | `DashboardSummaryResponse` | 200, 403 | Scope agence |
| Dashboards | `GET /api/v1/dashboards/agent` | AGENT | Indicateurs personnels agent. | Query: date | Aucun | `DashboardSummaryResponse` | 200, 403 | Agent connecté |
| Dashboards | `GET /api/v1/dashboards/client` | CLIENT | Synthèse client. | Aucun | Aucun | `DashboardSummaryResponse` | 200, 403 | Client connecté |
| Rapports | `GET /api/v1/reports/transfers` | ADMIN, MANAGER | Rapport transferts. | Query: `format=JSON|PDF|CSV`, dates, agencyId, corridorId | Aucun | JSON ou fichier | 200, 400, 403 | Scope agence |
| Rapports | `GET /api/v1/reports/agencies/{agencyId}` | ADMIN, MANAGER | Rapport agence. | Path: `agencyId`; Query: period, format | Aucun | `AgencyReportResponse` ou fichier | 200, 403, 404 | Manager limité à son agence |
| Rapports | `GET /api/v1/reports/commissions` | ADMIN, MANAGER | Rapport commissions. | Query: dates, agencyId, format | Aucun | JSON ou fichier | 200, 403 | Scope agence |
| Rapports | `GET /api/v1/reports/aml` | ADMIN | Rapport conformité AML. | Query: dates, format | Aucun | JSON ou fichier | 200, 403 | Admin uniquement |

---

### 14.19 Audit et journalisation

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Audit | `GET /api/v1/audit-logs` | ADMIN | Recherche dans le journal d’audit. | Query: `actorUserId`, `action`, `entityType`, `from`, `to`, page, size | Aucun | `PageResponse<AuditLogResponse>` | 200, 403 | Admin uniquement |
| Audit | `GET /api/v1/audit-logs/{id}` | ADMIN | Détail audit. | Path: `id` | Aucun | `AuditLogResponse` | 200, 403, 404 | Admin uniquement |
| Audit | `GET /api/v1/audit-logs/export` | ADMIN | Export audit PDF/CSV. | Query: format, dates | Aucun | Fichier | 200, 400, 403 | Admin uniquement |

---

### 14.20 Assistant intelligent / Chatbot

| Module | Méthode / URL | Rôles | Description | Paramètres | Requête JSON | Réponse JSON | Codes | Sécurité / DTOs |
|---|---|---|---|---|---|---|---|---|
| Chatbot | `POST /api/v1/chatbot/message` | CLIENT, AGENT, Public limité | Répond aux questions fréquentes : statut, frais, délais. | Aucun | `ChatbotRequest` | `ChatbotResponse` | 200, 400, 429 | Rate limit, pas de données sensibles sans auth |
| Chatbot | `POST /api/v1/chatbot/escalate` | CLIENT, AGENT | Escalade vers un agent humain simulé. | Aucun | `{ "conversationId": "abc", "reason": "hors périmètre" }` | `{ "escalated": true }` | 200, 400, 401 | Auth requise |

---

## 15. Règles métier principales

### 15.1 Transfert

- Un transfert ne peut être créé que si :
  - l’agence source est active ;
  - le corridor est actif ;
  - la devise source et la devise cible sont actives ;
  - un taux de change actif existe ;
  - une grille tarifaire active couvre le montant ;
  - les plafonds corridor/agence/client ne sont pas dépassés ;
  - le contrôle AML ne bloque pas l’opération.
- Le code de retrait est généré uniquement après confirmation du paiement à l’envoi.
- Le code de retrait n’est jamais stocké en clair : seul son hash est conservé.
- Un transfert payé ne peut plus être annulé.
- Un transfert expiré ne peut plus être payé.
- Un transfert bloqué AML ne peut pas être payé avant revue.

### 15.2 Frais et commissions

- Frais = `fixedFee + amount * percentageFee / 100`.
- Total à payer = `amount + feeAmount`.
- Montant reçu = `amount * exchangeRateApplied`.
- Commission agence = `feeAmount * agencyCommissionRate / 100`.
- Commission centrale = `feeAmount * centralCommissionRate / 100`.
- La somme des pourcentages agence + centrale doit être égale à 100% ou conforme à la règle choisie par l’équipe.
- Les tranches actives d’une même grille ne doivent pas se chevaucher.

### 15.3 Caisse

- Un agent ne peut payer un retrait que si sa caisse est ouverte.
- Un retrait crée un mouvement `CASH_OUT`.
- Une création de transfert confirmée crée un mouvement `CASH_IN`.
- La clôture compare le solde théorique et le montant compté.
- Tout écart de caisse est journalisé.

### 15.4 KYC / AML

- Un client sans KYC approuvé peut être limité ou bloqué selon le seuil choisi.
- Les numéros de pièces d’identité sont chiffrés en base.
- Une alerte AML est créée si :
  - montant supérieur à un seuil ;
  - bénéficiaire ou expéditeur proche d’une entrée de watchlist ;
  - répétition d’opérations suspectes ;
  - corridor considéré sensible.
- Les alertes à risque critique peuvent bloquer le transfert.

### 15.5 Mobile Money

- Le Mobile Money est simulé, sans intégration réelle.
- Un transfert Mobile Money doit avoir un numéro wallet valide.
- Les callbacks opérateur sont simulés via endpoint admin.
- La réconciliation compare les transferts envoyés avec un relevé fictif.

---

## 16. Sécurité applicative

### 16.1 JWT

- Access token : durée recommandée 1h.
- Refresh token : durée recommandée 7 jours.
- Claims minimaux : `sub`, `userId`, `role`, `email`, `iat`, `exp`.
- Le refresh token est stocké en base sous forme hashée.
- Rotation du refresh token à chaque `/refresh`.

### 16.2 BCrypt

- Les mots de passe sont hashés avec BCrypt force 12.
- Aucun mot de passe n’est journalisé.
- La validation impose longueur, majuscule, minuscule, chiffre et caractère spécial.

### 16.3 2FA OTP

- OTP recommandé : 6 chiffres pour login, 8 caractères alphanumériques pour opérations sensibles.
- Expiration : 5 minutes.
- Max tentatives : 3.
- Stockage : hash OTP, jamais OTP clair.
- Canaux : SMS simulé, email simulé.

### 16.4 Rôles et accès

- `ROLE_ADMIN` : accès global.
- `ROLE_MANAGER` : accès limité à son agence.
- `ROLE_AGENT` : opérations front-office et caisse.
- `ROLE_CLIENT` : ressources personnelles uniquement.

### 16.5 CORS

- Origines autorisées : domaine Angular local et domaine de production.
- Méthodes : `GET, POST, PUT, PATCH, DELETE, OPTIONS`.
- Headers autorisés : `Authorization, Content-Type, Accept-Language`.

### 16.6 Rate limiting

- Login : 5 tentatives / 10 minutes / IP + email.
- OTP : 3 tentatives / 5 minutes.
- Chatbot public : 20 messages / heure / IP.
- À implémenter via filtre `RateLimitFilter` et stockage mémoire ou table dédiée.

### 16.7 Chiffrement AES-256

- Données concernées : numéros CIN, passeport, document KYC, identité bénéficiaire.
- Implémentation : `AttributeConverter<String, String>` JPA.
- Clé AES lue depuis variable d’environnement, jamais commitée.

---

## 17. Journalisation et audit

Actions à journaliser obligatoirement :

- Login réussi / échec répété.
- Logout.
- Création, paiement, annulation et expiration de transfert.
- Modification des taux de change.
- Modification des grilles de frais.
- Création/suspension utilisateur.
- Validation/rejet KYC.
- Création/résolution alerte AML.
- Ouverture/clôture caisse.
- Mouvements manuels de caisse.

Chaque entrée d’audit doit contenir : acteur, action, ressource, IP, user-agent, horodatage et détails JSON.

---

## 18. Gestion KYC / AML

### KYC

- Upload document KYC par client ou agent.
- Contrôle automatisé simulé : format de numéro, type de document, présence fichier.
- Statuts : `PENDING`, `APPROVED`, `REJECTED`, `EXPIRED`.
- Revue par admin ou manager.

### AML

- Liste de surveillance fictive OFAC.
- Matching simple : nom, prénom, pays, similarité basique.
- Alertes selon seuils et comportements.
- Tableau de bord conformité : alertes ouvertes, risques hauts, transferts bloqués.

---

## 19. Gestion multi-devises et taux de change

- Les devises sont paramétrables par l’administrateur.
- Les taux actifs sont utilisés lors de la simulation et de la création du transfert.
- Le taux appliqué est figé dans le transfert pour éviter les incohérences futures.
- Chaque modification de taux crée un historique.
- Les montants sont stockés avec `BigDecimal`, jamais `double`.

---

## 20. Gestion des transferts, frais, commissions et caisse

### Cycle standard

1. L’agent simule les frais.
2. L’agent saisit l’expéditeur et le bénéficiaire.
3. Le système vérifie corridor, taux, frais, KYC et AML.
4. L’agent valide le paiement à l’envoi avec OTP.
5. Le système génère la référence et le code de retrait.
6. Le bénéficiaire se présente en agence destination.
7. L’agent destination vérifie code, identité et statut.
8. L’agent confirme le paiement.
9. Le système met à jour caisse, transfert, commission, audit et notifications.

---

## 21. Gestion des notifications

Types de notifications :

- Création transfert.
- Code de retrait envoyé à l’expéditeur.
- Transfert disponible.
- Transfert payé.
- Transfert annulé ou expiré.
- Document KYC validé/rejeté.
- Alerte AML pour admin/manager.

Canaux : email simulé, SMS simulé, push simulé.

---

## 22. Module Mobile Money simulé

Le module Mobile Money permet de simuler un envoi vers un wallet Orange Money, Wave ou M-Pesa.

Fonctions :

- Création d’un transfert Mobile Money lié à un transfert existant.
- Simulation d’envoi opérateur.
- Simulation callback succès/échec.
- Réconciliation quotidienne.
- Dashboard des transferts Mobile Money.

---

## 23. Swagger / OpenAPI 3

### Configuration sans Spring Boot

Classe recommandée : `OpenApiConfig`.

Responsabilités :

- Déclarer les métadonnées API : titre, version, description.
- Déclarer le schéma de sécurité Bearer JWT.
- Scanner les contrôleurs REST.
- Documenter les DTOs avec annotations OpenAPI.

Exemple de conventions :

```java
@Operation(summary = "Create transfer", description = "Creates a new money transfer")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Transfer created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "403", description = "Forbidden")
})
```

Endpoints Swagger attendus :

- `/swagger-ui/index.html`
- `/v3/api-docs`

---

## 24. Répartition du travail de l’équipe

La répartition suivante doit être conservée telle quelle dans le projet.

### M1 — Backend : Sécurité & Utilisateurs

Responsabilités :

- Configuration Spring MVC 6 manuelle
- Spring Security 6
- JWT access token / refresh token
- BCrypt
- Authentification, login, logout, refresh token
- 2FA / OTP
- Entités User, Admin, Agent, Manager, Client
- RefreshToken
- OtpVerification
- JournalAudit
- Rate limiting
- Chiffrement AES-256 des données sensibles
- Sécurisation des endpoints par rôle

### M2 — Backend : Métier & Finance

Responsabilités :

- Agence
- Corridor
- GrilleFrais
- Beneficiaire
- Transfert
- Caisse
- MouvementCaisse
- Commission
- RapportAgence
- Calcul des frais
- Génération du code de retrait
- Paiement d’un transfert
- Gestion de caisse
- Rapports d’agence
- Endpoints liés aux transferts, caisses, agences, corridors et grilles de frais

### M3 — Backend : Référentiel, KYC / AML & API

Responsabilités :

- Pays
- Devise
- TauxChange
- Historique des taux de change
- DocumentKyc
- AlerteAml
- ListeSurveillance
- Vérification OFAC fictive
- Alertes AML
- Notifications
- Configuration Swagger / OpenAPI 3
- CORS
- Gestion globale des exceptions
- Endpoints liés aux devises, pays, taux de change, KYC et alertes AML

### M4 — Frontend Angular 17+

Responsabilités :

- Architecture Angular
- Routing
- Lazy loading
- Guards par rôle
- Intercepteur JWT
- Refresh token automatique
- Module Auth
- Espace Admin
- Espace Manager
- Espace Agent
- Espace Client
- Dashboards Chart.js
- i18n français / anglais / arabe
- Interface responsive et accessible

### Responsabilités partagées

- Validation commune du schéma de base de données
- Revue de code via Pull Requests
- Conventions Git
- Réunions de synchronisation
- Jeu de données fictif commun
- Swagger documenté pour tous les endpoints
- Tests manuels avant soutenance

---

## 25. Livrables attendus

| Livrable | Description |
|---|---|
| Code source GitHub privé | Backend Spring MVC + Frontend Angular |
| README complet | Installation, configuration, lancement, comptes de test |
| Schéma base de données | MCD/MLD ou diagramme relationnel |
| Diagrammes UML | Cas d’utilisation, classes, séquences des scénarios principaux |
| Swagger UI fonctionnel | Tous les endpoints documentés |
| Jeu de données fictif | Pays, devises, agences, utilisateurs, taux, grilles, transferts |
| Présentation | Architecture, démo live, sécurité, répartition équipe |
| Tests manuels | Scénarios validés avant soutenance |

---

## 26. Conseils de structure Git et conventions de commits

### Branches recommandées

```text
main
├── develop
├── feature/m1-security-auth
├── feature/m1-users-otp
├── feature/m2-transfers
├── feature/m2-cash-commissions
├── feature/m3-referential-kyc-aml
├── feature/m3-swagger-exceptions
└── feature/m4-angular-frontend
```

### Conventions de commits

Format conseillé :

```text
<type>(<scope>): <message court>
```

Types :

- `feat`: nouvelle fonctionnalité
- `fix`: correction
- `docs`: documentation
- `refactor`: restructuration sans changement fonctionnel
- `test`: tests
- `chore`: configuration, dépendances, tâches techniques

Exemples :

```text
feat(auth): add jwt login endpoint
feat(transfer): implement fee simulation
fix(cash): prevent payout when register is closed
docs(api): document transfer endpoints in swagger
chore(config): add manual spring mvc initializer
```

### Pull Requests

Chaque PR doit contenir :

- Objectif de la fonctionnalité.
- Endpoints ajoutés ou modifiés.
- Tables impactées.
- Captures Swagger ou UI si utile.
- Tests manuels réalisés.
- Reviewer minimum : 1 membre.

---

## 27. Points critiques à vérifier avant la soutenance

### Sécurité

- Aucun secret dans Git.
- JWT expiré refusé.
- Refresh token révoqué inutilisable.
- Passwords hashés BCrypt.
- Numéros d’identité chiffrés en base.
- Rate limiting actif sur login et OTP.
- Rôles testés sur endpoints sensibles.
- CORS limité à l’URL Angular.

### Backend

- Application fonctionne sans Spring Boot.
- Configuration Spring MVC manuelle opérationnelle.
- Swagger UI accessible.
- Exceptions globales propres.
- Validation Jakarta active.
- Transactions `@Transactional` sur opérations critiques.
- Aucun contrôleur ne contient de logique métier lourde.

### Métier

- Simulation frais correcte.
- Taux appliqué figé dans le transfert.
- Code de retrait non stocké en clair.
- Transfert payé impossible à annuler.
- Transfert expiré impossible à payer.
- Caisse ouverte obligatoire pour paiement/retrait.
- Mouvements de caisse cohérents.
- Commissions calculées et consultables.

### KYC / AML

- Upload KYC testé.
- Validation/rejet KYC fonctionne.
- Watchlist fictive testée.
- Alerte AML générée sur seuil.
- Transfert bloqué si risque critique.

### Frontend

- Guards rôle fonctionnels.
- Intercepteur JWT actif.
- Refresh token automatique testé.
- Lazy loading fonctionnel.
- Dashboards Chart.js alimentés par API.
- i18n FR/EN/AR disponible.
- Interface responsive.

### Démo live

Scénario recommandé :

1. Connexion admin.
2. Création devise/pays/corridor/grille frais.
3. Création agence et agent.
4. Connexion agent.
5. Ouverture caisse.
6. Simulation transfert.
7. Création transfert.
8. Confirmation avec OTP.
9. Recherche par code de retrait.
10. Paiement du transfert.
11. Vérification caisse, commission, audit, notification.
12. Consultation dashboard admin ou manager.

---

## 28. Conclusion

Cette documentation fournit une base technique complète pour développer OkaneTransfer sous forme d’application web monolithique en couches. Elle respecte la stack imposée, la séparation des responsabilités, les contraintes de sécurité, les modules métier et la répartition fixée de l’équipe. Le contrat API proposé peut être utilisé directement comme base pour produire la spécification Swagger/OpenAPI 3 du backend.

