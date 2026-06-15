# OkaneTransfer - local PostgreSQL environment variables (Windows PowerShell)
# Usage: . .\scripts\set-dev-env.ps1

$env:OKANE_DB_DRIVER = "org.postgresql.Driver"
$env:OKANE_DB_URL = "jdbc:postgresql://localhost:5432/okane_transfer"
$env:OKANE_DB_USERNAME = "okane"
$env:OKANE_DB_PASSWORD = "okane"
$env:OKANE_HIBERNATE_DDL_AUTO = "update"
$env:OKANE_HIBERNATE_DIALECT = "org.hibernate.dialect.PostgreSQLDialect"

# Required for encrypted identity fields (must be exactly 32 characters)
$env:OKANE_AES_KEY = "OkaneTransferDevAesKey1234567890"

# Optional but recommended so JWT tokens survive server restarts
$env:OKANE_JWT_SECRET = "OkaneTransferDevJwtSecretKey1234567890"

Write-Host "OkaneTransfer dev environment variables loaded."
