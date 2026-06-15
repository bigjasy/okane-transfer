-- Part 2: run against okane_transfer database
-- psql -U postgres -d okane_transfer -f scripts/init-postgres-part2.sql

GRANT ALL ON SCHEMA public TO okane;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO okane;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO okane;
