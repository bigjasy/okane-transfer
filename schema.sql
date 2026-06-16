-- ============================================================
-- OkaneTransfer PostgreSQL Schema
-- Database: okane-transfer
-- Based on documentation-technique-okane-transfer.md
-- ============================================================

-- ============================================================
-- 1. ENUMS (as CHECK constraints / VARCHAR)
-- ============================================================

-- Role
-- ROLE_ADMIN, ROLE_MANAGER, ROLE_AGENT, ROLE_CLIENT

-- UserStatus
-- ACTIVE, SUSPENDED, DISABLED, PENDING_VERIFICATION

-- Language
-- FR, EN, AR

-- IdentityType
-- CIN, PASSPORT, RESIDENCE_CARD

-- KycStatus
-- NOT_SUBMITTED, PENDING, APPROVED, REJECTED, EXPIRED

-- OtpPurpose
-- LOGIN_2FA, TRANSFER_VALIDATION, PAYOUT_VALIDATION, PASSWORD_RESET

-- NotificationChannel
-- EMAIL, SMS, PUSH

-- NotificationStatus
-- PENDING, SENT, FAILED, READ

-- RateSource
-- MANUAL, EXTERNAL_API, SYSTEM

-- AgencyStatus
-- ACTIVE, SUSPENDED, CLOSED

-- TransferStatus
-- DRAFT, PENDING_PAYMENT, AVAILABLE, PAID, CANCELLED, EXPIRED, BLOCKED_AML

-- TransferChannel
-- AGENCY, ONLINE, MOBILE_MONEY

-- CashRegisterStatus
-- OPEN, CLOSED, BLOCKED

-- CashMovementType
-- CASH_IN, CASH_OUT, TRANSFER_SEND, TRANSFER_PAYOUT, ADJUSTMENT, CLOSING_DIFFERENCE

-- AmlAlertType
-- THRESHOLD_EXCEEDED, WATCHLIST_MATCH, REPEATED_ATTEMPTS, SUSPICIOUS_CORRIDOR

-- RiskLevel
-- LOW, MEDIUM, HIGH, CRITICAL

-- AmlAlertStatus
-- OPEN, UNDER_REVIEW, RESOLVED, FALSE_POSITIVE

-- KycDocumentType
-- CIN_FRONT, CIN_BACK, PASSPORT, PROOF_OF_ADDRESS

-- AuditAction
-- LOGIN, LOGOUT, CREATE_TRANSFER, PAY_TRANSFER, CANCEL_TRANSFER, UPDATE_RATE,
-- UPDATE_FEE_GRID, KYC_REVIEW, AML_REVIEW, CASH_CLOSING, USER_CREATED, USER_SUSPENDED

-- ============================================================
-- 2. TABLES
-- ============================================================

-- -----------------------------------------------------------
-- Referential: Currencies
-- -----------------------------------------------------------
CREATE TABLE currencies (
    id                      BIGSERIAL PRIMARY KEY,
    code                    VARCHAR(3) NOT NULL UNIQUE,
    name                    VARCHAR(100) NOT NULL,
    symbol                  VARCHAR(10),
    scale                   INTEGER NOT NULL DEFAULT 2,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Referential: Countries
-- -----------------------------------------------------------
CREATE TABLE countries (
    id                      BIGSERIAL PRIMARY KEY,
    iso_code                VARCHAR(3) NOT NULL UNIQUE,
    name                    VARCHAR(100) NOT NULL,
    phone_prefix            VARCHAR(5),
    currency_id             BIGINT REFERENCES currencies(id),
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Referential: Exchange Rates
-- -----------------------------------------------------------
CREATE TABLE exchange_rates (
    id                      BIGSERIAL PRIMARY KEY,
    source_currency_id      BIGINT NOT NULL REFERENCES currencies(id),
    target_currency_id      BIGINT NOT NULL REFERENCES currencies(id),
    rate                    NUMERIC(19, 8) NOT NULL,
    source                  VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    valid_from              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_different_currencies CHECK (source_currency_id != target_currency_id)
);

-- -----------------------------------------------------------
-- Agencies
-- -----------------------------------------------------------
CREATE TABLE agencies (
    id                      BIGSERIAL PRIMARY KEY,
    code                    VARCHAR(20) NOT NULL UNIQUE,
    name                    VARCHAR(100) NOT NULL,
    address                 VARCHAR(255),
    city                    VARCHAR(100),
    country_id              BIGINT REFERENCES countries(id),
    daily_limit             NUMERIC(19, 2),
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Corridors
-- -----------------------------------------------------------
CREATE TABLE corridors (
    id                      BIGSERIAL PRIMARY KEY,
    source_country_id       BIGINT NOT NULL REFERENCES countries(id),
    destination_country_id  BIGINT NOT NULL REFERENCES countries(id),
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    daily_limit             NUMERIC(19, 2),
    monthly_limit           NUMERIC(19, 2),
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_different_countries CHECK (source_country_id != destination_country_id)
);

-- -----------------------------------------------------------
-- Fee Grids
-- -----------------------------------------------------------
CREATE TABLE fee_grids (
    id                      BIGSERIAL PRIMARY KEY,
    corridor_id             BIGINT NOT NULL REFERENCES corridors(id),
    source_currency_id        BIGINT REFERENCES currencies(id),
    target_currency_id      BIGINT REFERENCES currencies(id),
    min_amount              NUMERIC(19, 2) NOT NULL,
    max_amount              NUMERIC(19, 2) NOT NULL,
    fixed_fee               NUMERIC(19, 2) NOT NULL DEFAULT 0,
    percentage_fee          NUMERIC(5, 4) NOT NULL DEFAULT 0,
    agency_commission_rate  NUMERIC(5, 4),
    central_commission_rate   NUMERIC(5, 4),
    valid_from              DATE NOT NULL,
    valid_to                DATE,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_min_max CHECK (min_amount <= max_amount),
    CONSTRAINT chk_valid_dates CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

-- -----------------------------------------------------------
-- Users (base table with JOINED inheritance)
-- -----------------------------------------------------------
CREATE TABLE users (
    id                      BIGSERIAL PRIMARY KEY,
    uuid                    VARCHAR(36) NOT NULL UNIQUE,
    email                   VARCHAR(180) NOT NULL UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    phone_number            VARCHAR(30),
    role                    VARCHAR(30) NOT NULL,
    status                  VARCHAR(40) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    two_factor_enabled      BOOLEAN NOT NULL DEFAULT FALSE,
    preferred_language      VARCHAR(5) NOT NULL DEFAULT 'FR',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at           TIMESTAMP,
    notify_email_enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    notify_sms_enabled      BOOLEAN NOT NULL DEFAULT TRUE,
    notify_push_enabled     BOOLEAN NOT NULL DEFAULT FALSE
);

-- -----------------------------------------------------------
-- Referential: Exchange Rate History
-- -----------------------------------------------------------
CREATE TABLE exchange_rate_histories (
    id                      BIGSERIAL PRIMARY KEY,
    source_currency_code    VARCHAR(3) NOT NULL,
    target_currency_code    VARCHAR(3) NOT NULL,
    old_rate                NUMERIC(19, 8) NOT NULL,
    new_rate                NUMERIC(19, 8) NOT NULL,
    source                  VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    changed_by_id           BIGINT REFERENCES users(id),
    changed_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Admins (extends User)
-- -----------------------------------------------------------
CREATE TABLE admins (
    id                      BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    department              VARCHAR(100),
    super_admin             BOOLEAN NOT NULL DEFAULT FALSE
);

-- -----------------------------------------------------------
-- Managers (extends User)
-- -----------------------------------------------------------
CREATE TABLE managers (
    id                      BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    agency_id               BIGINT REFERENCES agencies(id),
    approval_limit          NUMERIC(19, 2)
);

-- -----------------------------------------------------------
-- Agents (extends User)
-- -----------------------------------------------------------
CREATE TABLE agents (
    id                      BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    agency_id               BIGINT REFERENCES agencies(id),
    employee_code           VARCHAR(20) UNIQUE
    -- cash_register_id removed as it causes circular dependency; managed via foreign key in cash_registers
);

-- -----------------------------------------------------------
-- Clients (extends User)
-- -----------------------------------------------------------
CREATE TABLE clients (
    id                      BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    identity_type           VARCHAR(20),
    identity_number_encrypted VARCHAR(255),
    date_of_birth           DATE,
    country_id              BIGINT REFERENCES countries(id),
    kyc_status              VARCHAR(20) NOT NULL DEFAULT 'NOT_SUBMITTED'
);

-- -----------------------------------------------------------
-- Security: Refresh Tokens
-- -----------------------------------------------------------
CREATE TABLE refresh_tokens (
    id                      BIGSERIAL PRIMARY KEY,
    token_hash              VARCHAR(255) NOT NULL UNIQUE,
    user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at              TIMESTAMP NOT NULL,
    revoked                 BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at              TIMESTAMP,
    ip_address              VARCHAR(45),
    user_agent              TEXT
);

-- -----------------------------------------------------------
-- Security: OTP Verifications
-- -----------------------------------------------------------
CREATE TABLE otp_verifications (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    otp_hash                VARCHAR(255) NOT NULL,
    purpose                 VARCHAR(30) NOT NULL,
    channel                 VARCHAR(10) NOT NULL DEFAULT 'SMS',
    expires_at              TIMESTAMP NOT NULL,
    verified                BOOLEAN NOT NULL DEFAULT FALSE,
    attempt_count           INTEGER NOT NULL DEFAULT 0,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Audit Logs
-- -----------------------------------------------------------
CREATE TABLE audit_logs (
    id                      BIGSERIAL PRIMARY KEY,
    actor_user_id           BIGINT,
    actor_email             VARCHAR(180),
    action                  VARCHAR(30) NOT NULL,
    entity_type             VARCHAR(100),
    entity_id               VARCHAR(100),
    ip_address              VARCHAR(45),
    user_agent              TEXT,
    details_json            TEXT,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Beneficiaries
-- -----------------------------------------------------------
CREATE TABLE beneficiaries (
    id                      BIGSERIAL PRIMARY KEY,
    client_id               BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    phone_number            VARCHAR(30),
    country_id              BIGINT REFERENCES countries(id),
    identity_type           VARCHAR(20),
    identity_number_encrypted VARCHAR(255),
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Transfers
-- -----------------------------------------------------------
CREATE TABLE transfers (
    id                      BIGSERIAL PRIMARY KEY,
    reference               VARCHAR(20) NOT NULL UNIQUE,
    withdrawal_code_hash    VARCHAR(255),
    sender_id               BIGINT NOT NULL REFERENCES clients(id),
    beneficiary_id          BIGINT REFERENCES beneficiaries(id),
    source_agency_id        BIGINT REFERENCES agencies(id),
    destination_agency_id   BIGINT REFERENCES agencies(id),
    created_by_agent_id     BIGINT REFERENCES agents(id),
    source_country_id       BIGINT REFERENCES countries(id),
    destination_country_id  BIGINT REFERENCES countries(id),
    source_currency_id      BIGINT REFERENCES currencies(id),
    target_currency_id      BIGINT REFERENCES currencies(id),
    sent_amount             NUMERIC(19, 2) NOT NULL,
    fee_amount              NUMERIC(19, 2),
    exchange_rate_applied     NUMERIC(19, 8),
    received_amount           NUMERIC(19, 2),
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    channel                 VARCHAR(20) NOT NULL DEFAULT 'AGENCY',
    expires_at              TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at                 TIMESTAMP,
    cancelled_at            TIMESTAMP
);

-- -----------------------------------------------------------
-- Transfer Payments
-- -----------------------------------------------------------
CREATE TABLE transfer_payments (
    id                      BIGSERIAL PRIMARY KEY,
    transfer_id             BIGINT NOT NULL UNIQUE REFERENCES transfers(id) ON DELETE CASCADE,
    paid_by_agent_id        BIGINT REFERENCES agents(id),
    paid_at_agency_id       BIGINT REFERENCES agencies(id),
    beneficiary_identity_type VARCHAR(20),
    beneficiary_identity_number_encrypted VARCHAR(255),
    paid_amount             NUMERIC(19, 2),
    paid_at                 TIMESTAMP
);

-- -----------------------------------------------------------
-- Cash Registers
-- -----------------------------------------------------------
CREATE TABLE cash_registers (
    id                      BIGSERIAL PRIMARY KEY,
    agency_id               BIGINT NOT NULL REFERENCES agencies(id),
    agent_id                BIGINT REFERENCES agents(id),
    currency_id             BIGINT REFERENCES currencies(id),
    opening_balance         NUMERIC(19, 2) NOT NULL DEFAULT 0,
    current_balance         NUMERIC(19, 2) NOT NULL DEFAULT 0,
    status                  VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    opened_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at               TIMESTAMP
);

-- -----------------------------------------------------------
-- Cash Movements
-- -----------------------------------------------------------
CREATE TABLE cash_movements (
    id                      BIGSERIAL PRIMARY KEY,
    cash_register_id        BIGINT NOT NULL REFERENCES cash_registers(id) ON DELETE CASCADE,
    type                    VARCHAR(30) NOT NULL,
    amount                  NUMERIC(19, 2) NOT NULL,
    currency_id             BIGINT REFERENCES currencies(id),
    transfer_id             BIGINT REFERENCES transfers(id),
    reason                  VARCHAR(255),
    created_by_id           BIGINT REFERENCES users(id),
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Commissions
-- -----------------------------------------------------------
CREATE TABLE commissions (
    id                      BIGSERIAL PRIMARY KEY,
    transfer_id             BIGINT NOT NULL REFERENCES transfers(id) ON DELETE CASCADE,
    agency_id               BIGINT REFERENCES agencies(id),
    agency_part             NUMERIC(19, 2),
    central_part            NUMERIC(19, 2),
    currency_id             BIGINT REFERENCES currencies(id),
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Agency Reports
-- -----------------------------------------------------------
CREATE TABLE agency_reports (
    id                      BIGSERIAL PRIMARY KEY,
    agency_id               BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    report_period           VARCHAR(20) NOT NULL,
    total_transfers         BIGINT DEFAULT 0,
    total_volume            NUMERIC(19, 2) DEFAULT 0,
    total_fees              NUMERIC(19, 2) DEFAULT 0,
    total_commissions       NUMERIC(19, 2) DEFAULT 0,
    generated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- KYC Documents
-- -----------------------------------------------------------
CREATE TABLE kyc_documents (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_type           VARCHAR(20) NOT NULL,
    document_number_encrypted VARCHAR(255),
    file_path               VARCHAR(255),
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason        TEXT,
    uploaded_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by_id          BIGINT REFERENCES users(id),
    reviewed_at             TIMESTAMP
);

-- -----------------------------------------------------------
-- AML Alerts
-- -----------------------------------------------------------
CREATE TABLE aml_alerts (
    id                      BIGSERIAL PRIMARY KEY,
    transfer_id             BIGINT REFERENCES transfers(id) ON DELETE SET NULL,
    user_id                 BIGINT REFERENCES users(id) ON DELETE SET NULL,
    type                    VARCHAR(30) NOT NULL,
    risk_level              VARCHAR(20) NOT NULL DEFAULT 'LOW',
    status                  VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    description             TEXT,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by_id          BIGINT REFERENCES users(id),
    reviewed_at             TIMESTAMP
);

-- -----------------------------------------------------------
-- Watchlist Entries (OFAC)
-- -----------------------------------------------------------
CREATE TABLE watchlist_entries (
    id                      BIGSERIAL PRIMARY KEY,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    country_id              BIGINT REFERENCES countries(id),
    source                  VARCHAR(255),
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- Notifications
-- -----------------------------------------------------------
CREATE TABLE notifications (
    id                      BIGSERIAL PRIMARY KEY,
    recipient_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    channel                 VARCHAR(10) NOT NULL DEFAULT 'EMAIL',
    title                   VARCHAR(255) NOT NULL,
    message                 TEXT NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    related_entity_type     VARCHAR(100),
    related_entity_id         VARCHAR(100),
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at                 TIMESTAMP
);

-- -----------------------------------------------------------
-- Mobile Money Transfers
-- -----------------------------------------------------------
CREATE TABLE mobile_money_transfers (
    id                      BIGSERIAL PRIMARY KEY,
    transfer_id             BIGINT NOT NULL UNIQUE REFERENCES transfers(id) ON DELETE CASCADE,
    operator                VARCHAR(20) NOT NULL,
    wallet_phone_number     VARCHAR(20),
    operator_transaction_reference VARCHAR(255),
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reconciliation_status     VARCHAR(20) NOT NULL DEFAULT 'NOT_RECONCILED',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reconciled_at           TIMESTAMP
);

-- ============================================================
-- 3. INDEXES
-- ============================================================

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_uuid ON users(uuid);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);

CREATE INDEX idx_otp_user ON otp_verifications(user_id);
CREATE INDEX idx_otp_purpose ON otp_verifications(purpose);

CREATE INDEX idx_agencies_code ON agencies(code);
CREATE INDEX idx_agencies_country ON agencies(country_id);
CREATE INDEX idx_agencies_status ON agencies(status);

CREATE INDEX idx_corridors_src_dest ON corridors(source_country_id, destination_country_id);
CREATE INDEX idx_corridors_active ON corridors(active);

CREATE INDEX idx_fee_grids_corridor ON fee_grids(corridor_id);
CREATE INDEX idx_fee_grids_active ON fee_grids(active);
CREATE INDEX idx_fee_grids_amount ON fee_grids(min_amount, max_amount);

CREATE INDEX idx_exchange_rates_currencies ON exchange_rates(source_currency_id, target_currency_id);
CREATE INDEX idx_exchange_rates_active ON exchange_rates(active);

CREATE INDEX idx_countries_iso ON countries(iso_code);
CREATE INDEX idx_countries_currency ON countries(currency_id);

CREATE INDEX idx_currencies_code ON currencies(code);

CREATE INDEX idx_beneficiaries_client ON beneficiaries(client_id);
CREATE INDEX idx_beneficiaries_phone ON beneficiaries(phone_number);

CREATE INDEX idx_transfers_reference ON transfers(reference);
CREATE INDEX idx_transfers_sender ON transfers(sender_id);
CREATE INDEX idx_transfers_status ON transfers(status);
CREATE INDEX idx_transfers_created_at ON transfers(created_at);
CREATE INDEX idx_transfers_beneficiary ON transfers(beneficiary_id);

CREATE INDEX idx_cash_movements_register ON cash_movements(cash_register_id);
CREATE INDEX idx_cash_movements_type ON cash_movements(type);

CREATE INDEX idx_commissions_transfer ON commissions(transfer_id);
CREATE INDEX idx_commissions_agency ON commissions(agency_id);

CREATE INDEX idx_aml_alerts_status ON aml_alerts(status);
CREATE INDEX idx_aml_alerts_risk ON aml_alerts(risk_level);
CREATE INDEX idx_aml_alerts_transfer ON aml_alerts(transfer_id);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id);
CREATE INDEX idx_notifications_status ON notifications(status);

CREATE INDEX idx_kyc_user ON kyc_documents(user_id);
CREATE INDEX idx_kyc_status ON kyc_documents(status);

CREATE INDEX idx_audit_user ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

CREATE INDEX idx_mobile_money_transfer ON mobile_money_transfers(transfer_id);

CREATE INDEX idx_watchlist_active ON watchlist_entries(active);
CREATE INDEX idx_watchlist_name ON watchlist_entries(last_name, first_name);

-- ============================================================
-- 4. SEED DATA: Currencies
-- ============================================================
INSERT INTO currencies (code, name, symbol, scale, active) VALUES
('MAD', 'Moroccan Dirham', 'DH', 2, true),
('EUR', 'Euro', '€', 2, true),
('USD', 'US Dollar', '$', 2, true),
('GBP', 'British Pound', '£', 2, true),
('XOF', 'West African CFA Franc', 'CFA', 0, true),
('XAF', 'Central African CFA Franc', 'CFA', 0, true);

-- ============================================================
-- 5. SEED DATA: Countries
-- ============================================================
INSERT INTO countries (iso_code, name, phone_prefix, currency_id, active) VALUES
('MA', 'Morocco', '+212', (SELECT id FROM currencies WHERE code = 'MAD'), true),
('SN', 'Senegal', '+221', (SELECT id FROM currencies WHERE code = 'XOF'), true),
('CI', 'Ivory Coast', '+225', (SELECT id FROM currencies WHERE code = 'XOF'), true),
('FR', 'France', '+33', (SELECT id FROM currencies WHERE code = 'EUR'), true),
('US', 'United States', '+1', (SELECT id FROM currencies WHERE code = 'USD'), true),
('GB', 'United Kingdom', '+44', (SELECT id FROM currencies WHERE code = 'GBP'), true),
('CM', 'Cameroon', '+237', (SELECT id FROM currencies WHERE code = 'XAF'), true);

-- ============================================================
-- 6. SEED DATA: Exchange Rates (sample)
-- ============================================================
INSERT INTO exchange_rates (source_currency_id, target_currency_id, rate, source, active) VALUES
((SELECT id FROM currencies WHERE code = 'MAD'), (SELECT id FROM currencies WHERE code = 'XOF'), 61.0000, 'MANUAL', true),
((SELECT id FROM currencies WHERE code = 'EUR'), (SELECT id FROM currencies WHERE code = 'XOF'), 655.9570, 'MANUAL', true),
((SELECT id FROM currencies WHERE code = 'USD'), (SELECT id FROM currencies WHERE code = 'MAD'), 9.8500, 'MANUAL', true),
((SELECT id FROM currencies WHERE code = 'GBP'), (SELECT id FROM currencies WHERE code = 'MAD'), 12.4500, 'MANUAL', true),
((SELECT id FROM currencies WHERE code = 'MAD'), (SELECT id FROM currencies WHERE code = 'EUR'), 0.0920, 'MANUAL', true),
((SELECT id FROM currencies WHERE code = 'XOF'), (SELECT id FROM currencies WHERE code = 'MAD'), 0.0164, 'MANUAL', true),
((SELECT id FROM currencies WHERE code = 'XOF'), (SELECT id FROM currencies WHERE code = 'EUR'), 0.0015, 'MANUAL', true),
((SELECT id FROM currencies WHERE code = 'MAD'), (SELECT id FROM currencies WHERE code = 'USD'), 0.1015, 'MANUAL', true),
((SELECT id FROM currencies WHERE code = 'MAD'), (SELECT id FROM currencies WHERE code = 'GBP'), 0.0803, 'MANUAL', true),
((SELECT id FROM currencies WHERE code = 'EUR'), (SELECT id FROM currencies WHERE code = 'MAD'), 10.8696, 'MANUAL', true);

-- ============================================================
-- 7. SEED DATA: Agencies
-- ============================================================
INSERT INTO agencies (code, name, address, city, country_id, daily_limit, status) VALUES
('AGY001', 'Agence Casablanca Centre', '123 Bd Mohammed V', 'Casablanca', (SELECT id FROM countries WHERE iso_code = 'MA'), 500000.00, 'ACTIVE'),
('AGY002', 'Agence Rabat Ville', '45 Av. Mohammed VI', 'Rabat', (SELECT id FROM countries WHERE iso_code = 'MA'), 300000.00, 'ACTIVE'),
('AGY003', 'Agence Dakar Plateau', '12 Av. Lamine Gueye', 'Dakar', (SELECT id FROM countries WHERE iso_code = 'SN'), 250000.00, 'ACTIVE'),
('AGY004', 'Agence Abidjan Treichville', '78 Bd de la Republique', 'Abidjan', (SELECT id FROM countries WHERE iso_code = 'CI'), 200000.00, 'ACTIVE'),
('AGY005', 'Agence Paris Centre', '10 Rue de la Paix', 'Paris', (SELECT id FROM countries WHERE iso_code = 'FR'), 1000000.00, 'ACTIVE');

-- ============================================================
-- 8. SEED DATA: Corridors
-- ============================================================
INSERT INTO corridors (source_country_id, destination_country_id, active, daily_limit, monthly_limit) VALUES
((SELECT id FROM countries WHERE iso_code = 'MA'), (SELECT id FROM countries WHERE iso_code = 'SN'), true, 100000.00, 2000000.00),
((SELECT id FROM countries WHERE iso_code = 'MA'), (SELECT id FROM countries WHERE iso_code = 'CI'), true, 100000.00, 2000000.00),
((SELECT id FROM countries WHERE iso_code = 'FR'), (SELECT id FROM countries WHERE iso_code = 'MA'), true, 500000.00, 5000000.00),
((SELECT id FROM countries WHERE iso_code = 'US'), (SELECT id FROM countries WHERE iso_code = 'MA'), true, 300000.00, 3000000.00),
((SELECT id FROM countries WHERE iso_code = 'FR'), (SELECT id FROM countries WHERE iso_code = 'SN'), true, 200000.00, 2000000.00),
((SELECT id FROM countries WHERE iso_code = 'MA'), (SELECT id FROM countries WHERE iso_code = 'FR'), true, 100000.00, 2000000.00);

-- ============================================================
-- 9. SEED DATA: Fee Grids
-- ============================================================
INSERT INTO fee_grids (corridor_id, source_currency_id, target_currency_id, min_amount, max_amount, fixed_fee, percentage_fee, agency_commission_rate, central_commission_rate, valid_from, active) VALUES
-- Corridor MA -> SN (MAD -> XOF)
((SELECT c.id FROM corridors c JOIN countries src ON c.source_country_id = src.id JOIN countries dst ON c.destination_country_id = dst.id WHERE src.iso_code = 'MA' AND dst.iso_code = 'SN'),
 (SELECT id FROM currencies WHERE code = 'MAD'), (SELECT id FROM currencies WHERE code = 'XOF'),
 0, 5000, 50.00, 0.03, 0.30, 0.70, '2025-01-01', true),
((SELECT c.id FROM corridors c JOIN countries src ON c.source_country_id = src.id JOIN countries dst ON c.destination_country_id = dst.id WHERE src.iso_code = 'MA' AND dst.iso_code = 'SN'),
 (SELECT id FROM currencies WHERE code = 'MAD'), (SELECT id FROM currencies WHERE code = 'XOF'),
 5000, 20000, 30.00, 0.025, 0.35, 0.65, '2025-01-01', true),
-- Corridor FR -> MA (EUR -> MAD)
((SELECT c.id FROM corridors c JOIN countries src ON c.source_country_id = src.id JOIN countries dst ON c.destination_country_id = dst.id WHERE src.iso_code = 'FR' AND dst.iso_code = 'MA'),
 (SELECT id FROM currencies WHERE code = 'EUR'), (SELECT id FROM currencies WHERE code = 'MAD'),
 0, 1000, 5.00, 0.015, 0.40, 0.60, '2025-01-01', true),
((SELECT c.id FROM corridors c JOIN countries src ON c.source_country_id = src.id JOIN countries dst ON c.destination_country_id = dst.id WHERE src.iso_code = 'FR' AND dst.iso_code = 'MA'),
 (SELECT id FROM currencies WHERE code = 'EUR'), (SELECT id FROM currencies WHERE code = 'MAD'),
 1000, 10000, 3.00, 0.01, 0.45, 0.55, '2025-01-01', true),
-- Corridor MA -> FR (MAD -> EUR)
((SELECT c.id FROM corridors c JOIN countries src ON c.source_country_id = src.id JOIN countries dst ON c.destination_country_id = dst.id WHERE src.iso_code = 'MA' AND dst.iso_code = 'FR'),
 (SELECT id FROM currencies WHERE code = 'MAD'), (SELECT id FROM currencies WHERE code = 'EUR'),
 0, 5000, 30.00, 0.025, 0.35, 0.65, '2025-01-01', true),
((SELECT c.id FROM corridors c JOIN countries src ON c.source_country_id = src.id JOIN countries dst ON c.destination_country_id = dst.id WHERE src.iso_code = 'MA' AND dst.iso_code = 'FR'),
 (SELECT id FROM currencies WHERE code = 'MAD'), (SELECT id FROM currencies WHERE code = 'EUR'),
 5000, 20000, 20.00, 0.02, 0.40, 0.60, '2025-01-01', true);

-- ============================================================
-- End of Schema
-- ============================================================
