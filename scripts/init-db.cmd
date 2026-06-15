@echo off
REM OkaneTransfer - init PostgreSQL from CMD
REM Usage:
REM   cd c:\Users\hp\Downloads\okane-transfer
REM   scripts\init-db.cmd

set PSQL="C:\Program Files\PostgreSQL\17\bin\psql.exe"
if not exist %PSQL% set PSQL=psql

echo === OkaneTransfer - PostgreSQL init ===
echo You will be asked for the postgres superuser password.
echo.

echo [1/2] Creating database and user...
%PSQL% -U postgres -f scripts\init-postgres-part1.sql
echo.

echo [2/2] Granting schema privileges...
%PSQL% -U postgres -d okane_transfer -f scripts\init-postgres-part2.sql
echo.

echo Done. Database: okane_transfer ^| User: okane ^| Password: okane
echo Next: scripts\set-dev-env.cmd
echo Then: mvn clean package
pause
