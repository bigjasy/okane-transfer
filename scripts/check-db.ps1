# Quick check: PostgreSQL connection + tables + demo users
# Usage: . .\scripts\set-dev-env.ps1   (not required for psql check)
#        .\scripts\check-db.ps1

$ErrorActionPreference = "Stop"
$psql = "C:\Program Files\PostgreSQL\17\bin\psql.exe"
if (-not (Test-Path $psql)) {
    $found = Get-Command psql -ErrorAction SilentlyContinue
    if ($found) { $psql = $found.Source } else { throw "psql not found." }
}

Write-Host "=== Checking okane_transfer database ===" -ForegroundColor Cyan
Write-Host "Password for user okane: okane"
Write-Host ""

& $psql -U okane -d okane_transfer -c "\dt"
Write-Host ""
& $psql -U okane -d okane_transfer -c "SELECT email, role, status FROM users ORDER BY email;"
Write-Host ""
& $psql -U okane -d okane_transfer -c "SELECT code, name FROM agencies ORDER BY code;"
