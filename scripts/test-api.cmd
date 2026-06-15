@echo off
REM Quick smoke test after Tomcat is running.
REM Usage:
REM   scripts\test-api.cmd
REM   scripts\test-api.cmd http://localhost:8080/okane_transfer_war

set BASE=%1
if "%BASE%"=="" set BASE=http://localhost:8080/okane_transfer_war

echo {"email":"admin@okane.ma","password":"Password@123"}> "%TEMP%\okane-login-body.json"

echo.
echo === 1) OpenAPI document ===
curl.exe -s -o "%TEMP%\okane-api-docs.json" -w "HTTP %%{http_code}\n" "%BASE%/v3/api-docs"
findstr /C:"\"openapi\"" "%TEMP%\okane-api-docs.json" >nul
if errorlevel 1 (
    echo ERROR: /v3/api-docs did not return valid OpenAPI JSON.
    type "%TEMP%\okane-api-docs.json"
    exit /b 1
)
findstr /C:"/api/v1/auth/login" "%TEMP%\okane-api-docs.json" >nul
if errorlevel 1 (
    echo ERROR: login endpoint missing from OpenAPI paths.
    exit /b 1
)
echo OK: OpenAPI contains /api/v1/auth/login

echo.
echo === 2) Login (POST) ===
curl.exe -s -o "%TEMP%\okane-login.json" -w "HTTP %%{http_code}\n" ^
  -H "Content-Type: application/json" ^
  -d @%TEMP%\okane-login-body.json ^
  "%BASE%/api/v1/auth/login"
findstr /C:"accessToken" "%TEMP%\okane-login.json" >nul
if errorlevel 1 (
    echo ERROR: login did not return accessToken.
    type "%TEMP%\okane-login.json"
    exit /b 1
)
echo OK: login returned accessToken

echo.
echo === 3) Swagger UI ===
curl.exe -s -o nul -w "HTTP %%{http_code}\n" "%BASE%/swagger-ui/index.html"

echo.
echo All checks passed.
echo Swagger UI: %BASE%/swagger-ui/index.html
