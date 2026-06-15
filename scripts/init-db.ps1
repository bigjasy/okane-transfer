# Creates PostgreSQL database and user for OkaneTransfer from the command line.
# Usage (PowerShell):
#   cd c:\Users\hp\Downloads\okane-transfer
#   .\scripts\init-db.ps1

$ErrorActionPreference = "Stop"

$psql = "C:\Program Files\PostgreSQL\17\bin\psql.exe"
if (-not (Test-Path $psql)) {
    $found = Get-Command psql -ErrorAction SilentlyContinue
    if ($found) { $psql = $found.Source } else { throw "psql not found. Install PostgreSQL or add it to PATH." }
}

$projectRoot = Split-Path $PSScriptRoot -Parent
$part1 = Join-Path $PSScriptRoot "init-postgres-part1.sql"
$part2 = Join-Path $PSScriptRoot "init-postgres-part2.sql"

Write-Host "=== OkaneTransfer - PostgreSQL init ===" -ForegroundColor Cyan
Write-Host "You will be asked for the postgres superuser password (set during PostgreSQL install)."
Write-Host ""

Write-Host "[1/2] Creating database and user..." -ForegroundColor Yellow
& $psql -U postgres -f $part1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Note: if database/user already exists, you can continue." -ForegroundColor DarkYellow
}

Write-Host "[2/2] Granting schema privileges..." -ForegroundColor Yellow
& $psql -U postgres -d okane_transfer -f $part2
if ($LASTEXITCODE -ne 0) { throw "Failed to grant schema privileges." }

Write-Host ""
Write-Host "Done. Database: okane_transfer | User: okane | Password: okane" -ForegroundColor Green
Write-Host "Next: . .\scripts\set-dev-env.ps1" -ForegroundColor Green
Write-Host "Then:  mvn clean package" -ForegroundColor Green
