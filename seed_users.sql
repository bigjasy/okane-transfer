-- ============================================================
-- OkaneTransfer test users seed
-- Password for all accounts: Password@123
-- ============================================================

INSERT INTO users (
    uuid,
    email,
    password_hash,
    first_name,
    last_name,
    role,
    status,
    preferred_language
) VALUES
(
    '00000000-0000-4000-8000-000000000001',
    'admin@okane.ma',
    '$2a$12$7zh3qAsI65ehgc9vGo2Fher6P0ybHOrVn02MBV5LK.3F3JKfSDocu',
    'Admin',
    'Okane',
    'ROLE_ADMIN',
    'ACTIVE',
    'FR'
),
(
    '00000000-0000-4000-8000-000000000002',
    'manager@okane.ma',
    '$2a$12$7zh3qAsI65ehgc9vGo2Fher6P0ybHOrVn02MBV5LK.3F3JKfSDocu',
    'Manager',
    'Okane',
    'ROLE_MANAGER',
    'ACTIVE',
    'FR'
),
(
    '00000000-0000-4000-8000-000000000003',
    'agent@okane.ma',
    '$2a$12$7zh3qAsI65ehgc9vGo2Fher6P0ybHOrVn02MBV5LK.3F3JKfSDocu',
    'Agent',
    'Okane',
    'ROLE_AGENT',
    'ACTIVE',
    'FR'
),
(
    '00000000-0000-4000-8000-000000000004',
    'client@okane.ma',
    '$2a$12$7zh3qAsI65ehgc9vGo2Fher6P0ybHOrVn02MBV5LK.3F3JKfSDocu',
    'Client',
    'Okane',
    'ROLE_CLIENT',
    'ACTIVE',
    'FR'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO admins (id, department, super_admin)
SELECT id, 'Administration', TRUE
FROM users
WHERE email = 'admin@okane.ma'
  AND role = 'ROLE_ADMIN'
ON CONFLICT (id) DO NOTHING;

INSERT INTO managers (id, agency_id, approval_limit)
SELECT id, 1, 100000.00
FROM users
WHERE email = 'manager@okane.ma'
  AND role = 'ROLE_MANAGER'
ON CONFLICT (id) DO NOTHING;

INSERT INTO agents (id, agency_id, employee_code)
SELECT id, 1, 'AGT-TEST-001'
FROM users
WHERE email = 'agent@okane.ma'
  AND role = 'ROLE_AGENT'
ON CONFLICT (id) DO NOTHING;

INSERT INTO clients (id, country_id, kyc_status)
SELECT
    id,
    COALESCE((SELECT countries.id FROM countries WHERE countries.iso_code = 'MA' LIMIT 1), 1),
    'NOT_SUBMITTED'
FROM users
WHERE email = 'client@okane.ma'
  AND role = 'ROLE_CLIENT'
ON CONFLICT (id) DO NOTHING;

UPDATE users SET phone_number = '+212600000001', notify_email_enabled = TRUE, notify_sms_enabled = TRUE WHERE email = 'admin@okane.ma';
UPDATE users SET phone_number = '+212600000002', notify_email_enabled = TRUE, notify_sms_enabled = TRUE WHERE email = 'manager@okane.ma';
UPDATE users SET phone_number = '+212600000003', notify_email_enabled = TRUE, notify_sms_enabled = TRUE WHERE email = 'agent@okane.ma';
UPDATE users SET phone_number = '+212600000004', notify_email_enabled = TRUE, notify_sms_enabled = TRUE WHERE email = 'client@okane.ma';
