-- Run once on existing databases created before real notification delivery support.
ALTER TABLE users ADD COLUMN IF NOT EXISTS notify_email_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS notify_sms_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS notify_push_enabled BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users SET phone_number = '+212600000001' WHERE email = 'admin@okane.ma' AND (phone_number IS NULL OR phone_number = '');
UPDATE users SET phone_number = '+212600000002' WHERE email = 'manager@okane.ma' AND (phone_number IS NULL OR phone_number = '');
UPDATE users SET phone_number = '+212600000003' WHERE email = 'agent@okane.ma' AND (phone_number IS NULL OR phone_number = '');
UPDATE users SET phone_number = '+212600000004' WHERE email = 'client@okane.ma' AND (phone_number IS NULL OR phone_number = '');
