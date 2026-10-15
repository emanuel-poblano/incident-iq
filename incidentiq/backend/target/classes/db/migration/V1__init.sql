-- V1__init.sql
--
-- Phase 1 placeholder migration. This lets the application boot against a
-- fresh PostgreSQL database with Flyway wired up correctly before the real
-- schema (Incident, Investigation, Evidence, TimelineEvent, Recommendation)
-- is introduced in Phase 2.

CREATE TABLE IF NOT EXISTS schema_bootstrap (
    id   SERIAL PRIMARY KEY,
    note VARCHAR(255) NOT NULL
);

INSERT INTO schema_bootstrap (note) VALUES ('IncidentIQ schema initialized');
