-- Part 1: run as postgres superuser (no database selected)
-- psql -U postgres -f scripts/init-postgres-part1.sql

CREATE DATABASE okane_transfer;

CREATE USER okane WITH PASSWORD 'okane';

GRANT ALL PRIVILEGES ON DATABASE okane_transfer TO okane;
