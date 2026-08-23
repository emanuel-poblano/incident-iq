# Infrastructure

Reserved for supporting infra configuration that doesn't belong in
`docker-compose.yml` directly — e.g. database bootstrap scripts, or later an
OpenSearch compose overlay when that integration is added (see spec §23,
not built in the MVP).

Currently empty: schema management is owned entirely by Flyway migrations in
`backend/src/main/resources/db/migration`.
