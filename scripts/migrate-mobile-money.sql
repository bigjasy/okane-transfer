-- Mobile Money module table (run once on existing okane_transfer DB)
-- Usage: psql -h localhost -U okane -d okane_transfer -f scripts/migrate-mobile-money.sql

CREATE TABLE IF NOT EXISTS mobile_money_transfers (
    id                      BIGSERIAL PRIMARY KEY,
    transfer_id             BIGINT NOT NULL UNIQUE REFERENCES transfers(id) ON DELETE CASCADE,
    operator                VARCHAR(20) NOT NULL,
    wallet_phone_number     VARCHAR(20),
    operator_transaction_reference VARCHAR(255),
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reconciliation_status   VARCHAR(20) NOT NULL DEFAULT 'NOT_RECONCILED',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reconciled_at           TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mobile_money_transfer ON mobile_money_transfers(transfer_id);
