#!/bin/bash
set -euo pipefail

PORT="${PORT:-8080}"

# Render Postgres: postgresql://user:pass@host:5432/dbname
if [ -n "${DATABASE_URL:-}" ]; then
  eval "$(python3 - <<'PY'
import os, urllib.parse
url = os.environ.get("DATABASE_URL", "")
if not url:
    raise SystemExit(0)
u = urllib.parse.urlparse(url)
host = u.hostname or "localhost"
port = u.port or 5432
db = (u.path or "/okane_transfer").lstrip("/") or "okane_transfer"
user = urllib.parse.unquote(u.username or "")
password = urllib.parse.unquote(u.password or "")
jdbc = f"jdbc:postgresql://{host}:{port}/{db}?sslmode=require"
print(f'export DB_JDBC_URL="{jdbc}"')
print(f'export DB_USER="{user}"')
print(f'export DB_PASSWORD="{password}"')
PY
)"
fi

DB_JDBC_URL="${DB_JDBC_URL:-${DB_URL:-jdbc:postgresql://localhost:5432/okane_transfer}}"
DB_USER="${DB_USER:-${DB_USERNAME:-okane}}"
DB_PASSWORD="${DB_PASSWORD:-okane}"

AES_KEY="${AES_KEY:-OkaneTransferDevAesKey1234567890}"
JWT_SECRET="${JWT_SECRET:-OkaneTransferDevJwtSecretKey1234567890}"
CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:4200}"
KYC_UPLOAD_DIR="${KYC_UPLOAD_DIR:-/tmp/okane-kyc}"
NOTIFICATION_DEV_EXPOSE_OTP="${NOTIFICATION_DEV_EXPOSE_OTP:-false}"
NOTIFICATION_EMAIL_ENABLED="${NOTIFICATION_EMAIL_ENABLED:-false}"
NOTIFICATION_SMS_ENABLED="${NOTIFICATION_SMS_ENABLED:-false}"
OPENROUTER_API_KEY="${OPENROUTER_API_KEY:-}"
OPENROUTER_MODEL="${OPENROUTER_MODEL:-openrouter/free}"
# smtp = local dev; sendgrid = Render free tier (SMTP ports 587/465 are blocked)
MAIL_PROVIDER="${MAIL_PROVIDER:-smtp}"
SENDGRID_API_KEY="${SENDGRID_API_KEY:-}"

# Only override classpath mail.* when Render env vars are set (empty -D wipes application.properties)
MAIL_JVM_OPTS=""
if [ -n "${MAIL_HOST:-}" ]; then MAIL_JVM_OPTS="${MAIL_JVM_OPTS} -Dmail.host=${MAIL_HOST}"; fi
if [ -n "${MAIL_PORT:-}" ]; then MAIL_JVM_OPTS="${MAIL_JVM_OPTS} -Dmail.port=${MAIL_PORT}"; fi
if [ -n "${MAIL_USERNAME:-}" ]; then MAIL_JVM_OPTS="${MAIL_JVM_OPTS} -Dmail.username=${MAIL_USERNAME}"; fi
if [ -n "${MAIL_PASSWORD:-}" ]; then MAIL_JVM_OPTS="${MAIL_JVM_OPTS} -Dmail.password=${MAIL_PASSWORD}"; fi
if [ -n "${MAIL_FROM:-}" ]; then MAIL_JVM_OPTS="${MAIL_JVM_OPTS} -Dmail.from=${MAIL_FROM}"; fi
if [ -n "${MAIL_STARTTLS:-}" ]; then MAIL_JVM_OPTS="${MAIL_JVM_OPTS} -Dmail.starttls=${MAIL_STARTTLS}"; fi
if [ -n "${MAIL_PROVIDER:-}" ]; then MAIL_JVM_OPTS="${MAIL_JVM_OPTS} -Dmail.provider=${MAIL_PROVIDER}"; fi
if [ -n "${SENDGRID_API_KEY:-}" ]; then MAIL_JVM_OPTS="${MAIL_JVM_OPTS} -Dsendgrid.api.key=${SENDGRID_API_KEY}"; fi

mkdir -p "$KYC_UPLOAD_DIR"

if [ "${INIT_DB:-false}" = "true" ] && [ -n "${DATABASE_URL:-}" ]; then
  USERS_TABLE=$(psql "$DATABASE_URL" -tAc "SELECT to_regclass('public.users') IS NOT NULL" 2>/dev/null || echo "f")
  if [ "$USERS_TABLE" != "t" ]; then
    echo "Fresh database — running schema + seed..."
    set +e
    psql "$DATABASE_URL" -v ON_ERROR_STOP=0 -f /app/db/schema.sql
    psql "$DATABASE_URL" -v ON_ERROR_STOP=0 -f /app/db/seed_users.sql
    set -e
  else
    echo "Database already initialized — skipping schema/seed."
  fi
fi

# Tomcat listens on Render PORT
sed -i.bak "s/port=\"8080\"/port=\"${PORT}\"/" "$CATALINA_HOME/conf/server.xml"

export CATALINA_OPTS="${CATALINA_OPTS:-} \
  -Ddb.url=${DB_JDBC_URL} \
  -Ddb.username=${DB_USER} \
  -Ddb.password=${DB_PASSWORD} \
  -Ddb.driver=org.postgresql.Driver \
  -Dhibernate.ddl-auto=none \
  -Dhibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
  -Dhibernate.show-sql=false \
  -Daes.key=${AES_KEY} \
  -Djwt.secret=${JWT_SECRET} \
  -Dopenrouter.api.key=${OPENROUTER_API_KEY} \
  -Dopenrouter.model=${OPENROUTER_MODEL} \
  -Dcors.allowed-origins=${CORS_ALLOWED_ORIGINS} \
  -Dkyc.upload-dir=${KYC_UPLOAD_DIR} \
  -Daml.threshold=10000 \
  -Dexchange-rate.external.provider=OkaneFX-Simulated \
  -Dnotification.dev.expose-otp=${NOTIFICATION_DEV_EXPOSE_OTP} \
  -Dnotification.email.enabled=${NOTIFICATION_EMAIL_ENABLED} \
  -Dnotification.sms.enabled=${NOTIFICATION_SMS_ENABLED} \
  ${MAIL_JVM_OPTS} \
  -Dtwilio.account-sid=${TWILIO_ACCOUNT_SID:-} \
  -Dtwilio.auth-token=${TWILIO_AUTH_TOKEN:-} \
  -Dtwilio.from-number=${TWILIO_FROM_NUMBER:-}"

echo "Starting Tomcat on port ${PORT}..."
exec catalina.sh run
