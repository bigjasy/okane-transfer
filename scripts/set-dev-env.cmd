@echo off
REM OkaneTransfer - environment variables for CMD
REM Usage:
REM   cd c:\Users\hp\Downloads\okane-transfer
REM   scripts\set-dev-env.cmd

set OKANE_DB_DRIVER=org.postgresql.Driver
set OKANE_DB_URL=jdbc:postgresql://localhost:5432/okane_transfer
set OKANE_DB_USERNAME=okane
set OKANE_DB_PASSWORD=okane
set OKANE_HIBERNATE_DDL_AUTO=update
set OKANE_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
set OKANE_AES_KEY=OkaneTransferDevAesKey1234567890
set OKANE_JWT_SECRET=OkaneTransferDevJwtSecretKey1234567890

echo OkaneTransfer dev environment variables loaded.
