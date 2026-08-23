# IncidentIQ

AI-powered incident investigation for software engineering teams.

> Give IncidentIQ an application incident and its available evidence, and it
> produces an evidence-backed probable root cause, confidence score,
> timeline, and recommended next steps.

IncidentIQ is **not** a replacement for Splunk, OpenSearch, Datadog, Jira, or
PagerDuty. It sits on top of your existing engineering data and helps
engineers investigate incidents faster once they've already been detected.

## Status

🚧 **Phase 1 — Scaffolding.** Project structure, backend skeleton, frontend
skeleton, and Docker Compose wiring are in place. Business logic (Incident
CRUD, AI investigation, results UI, seed data, tests) is implemented in the
phases that follow — see [Roadmap](#roadmap).

## Problem being solved

When an incident happens, engineers spend a large share of their time just
*assembling the evidence* — grepping logs, cross-referencing deploys, staring
at timelines — before they can even start reasoning about root cause.
IncidentIQ automates the evidence-gathering-to-hypothesis step and keeps every
conclusion traceable back to the evidence that produced it.

## MVP features

1. **Dashboard** — incident counts by status, recent incidents.
2. **Create Incident** — title, description, service, environment, severity, pasted logs.
3. **Incident Details** — full incident info, raw log viewer, "Investigate Incident" action.
4. **AI Investigation** — structured JSON output: root cause, confidence, evidence, timeline, recommendations.
5. **Investigation Results** — dedicated results view distinguishing *Observed Evidence* from *Probable Root Cause*.
6. **Investigation History** — every investigation of an incident is stored and browsable.

## Architecture

See [`docs/architecture.md`](docs/architecture.md) for diagrams. In short:

```
incidentiq/
├── frontend/         Next.js (App Router) + TypeScript + Tailwind
├── backend/          Spring Boot (Java 21) + Spring Data JPA + Maven
├── infrastructure/   supporting infra config (db init, etc.)
├── docs/             architecture notes
├── docker-compose.yml
└── .env.example
```

The backend is layered as `controller → service → repository`, with DTOs at
every API boundary (entities are never returned directly). The AI provider is
abstracted behind `AIInvestigationService` so the underlying LLM vendor can be
swapped without touching business logic.

## Technology stack

| Layer      | Choice                                             |
|------------|-----------------------------------------------------|
| Frontend   | Next.js, TypeScript, React, Tailwind CSS            |
| Backend    | Java 21, Spring Boot 3, Spring Web, Spring Data JPA |
| Database   | PostgreSQL, Flyway migrations                        |
| AI         | Provider-agnostic via `AIInvestigationService`      |
| Infra      | Docker Compose                                       |

## Local setup

### Prerequisites

- Docker + Docker Compose
- (for non-Docker local dev) Java 21, Maven, Node.js 20+, PostgreSQL 16

### Quick start (Docker)

```bash
cp .env.example .env
# edit .env and set AI_API_KEY (and DB credentials if desired)
docker compose up
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- Health check: http://localhost:8080/actuator/health

### Running without Docker

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend (separate terminal)
cd frontend
npm install
npm run dev
```

## Environment variables

See [`.env.example`](.env.example) for the full list:

- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- `AI_PROVIDER`, `AI_API_KEY`, `AI_MODEL`
- `BACKEND_URL`, `NEXT_PUBLIC_API_URL`

Never commit a real `.env` file — only `.env.example` is checked in.

## API endpoints (MVP)

```
POST   /api/incidents
GET    /api/incidents
GET    /api/incidents/{id}
PUT    /api/incidents/{id}
DELETE /api/incidents/{id}

POST   /api/incidents/{id}/investigate

GET    /api/incidents/{id}/investigations
GET    /api/investigations/{id}

GET    /actuator/health
```

*(Incident/Investigation endpoints are implemented in Phase 3 and Phase 6.)*

## Demo scenarios

Once seed data is implemented (Phase 9), the app ships with 5 realistic
incidents out of the box: database connection pool exhaustion, external
payment API timeout, memory leak crash, bad-deployment HTTP 500s, and an
expired TLS certificate — each with logs and an expected probable root cause,
so the product can be demoed without any external integrations.

## Roadmap

| Phase | Scope                                  | Status |
|-------|------------------------------------------|--------|
| 1     | Project scaffolding                       | ✅ done |
| 2     | PostgreSQL + JPA entities                 | ⏳ next |
| 3     | Incident REST API                         | pending |
| 4     | Next.js dashboard                         | pending |
| 5     | Incident creation/detail UI               | pending |
| 6     | AI investigation service                  | pending |
| 7     | Investigation result UI                   | pending |
| 8     | Investigation history                     | pending |
| 9     | Demo/seed data                            | pending |
| 10    | Testing                                   | pending |
| 11    | Docker Compose polish                     | pending |
| 12    | Final polish                              | pending |

Beyond the MVP (not built yet, by design): OpenSearch/Datadog/Splunk log
providers, Prometheus/Grafana, GitHub/GitLab/Jira/Slack/ServiceNow
integrations, AWS/Kubernetes awareness, historical-incident RAG, automated
remediation, runbooks, notifications, SSO, RBAC, multi-tenancy, billing.
