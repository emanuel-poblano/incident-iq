# IncidentIQ Architecture

## Product flow

```mermaid
flowchart TD
    A[Engineer creates incident] --> B[Pastes raw logs as evidence]
    B --> C[POST /api/incidents]
    C --> D[(PostgreSQL: Incident)]
    A2[Engineer clicks Investigate] --> E[POST /api/incidents/id/investigate]
    E --> F[InvestigationService gathers evidence]
    F --> G[AIInvestigationService]
    G --> H[LLM provider]
    H --> G
    G --> I[Validate structured response]
    I --> D2[(PostgreSQL: Investigation, Evidence, Timeline, Recommendations)]
    D2 --> J[Investigation results UI]
```

## System components

```mermaid
flowchart LR
    subgraph Frontend [Next.js]
        UI[Dashboard / Incident / Investigation views]
    end

    subgraph Backend [Spring Boot]
        Ctrl[Controllers]
        Svc[Services]
        Repo[JPA Repositories]
        AI[AIInvestigationService]
        LP[LogProvider]
    end

    DB[(PostgreSQL)]
    Provider[LLM Provider API]

    UI -->|REST/JSON| Ctrl
    Ctrl --> Svc
    Svc --> Repo
    Svc --> AI
    Svc --> LP
    Repo --> DB
    AI --> Provider
```

## Extensibility seams

- **`AIInvestigationService`** — swap the LLM provider without touching controllers, services, or the DB schema.
- **`LogProvider`** — MVP ships a pasted-log implementation only; `OpenSearchLogProvider` / `DatadogLogProvider` / `SplunkLogProvider` can be added later behind the same interface.
- **DTOs everywhere at the API boundary** — entities are never returned directly, so the schema can evolve without breaking the frontend contract.

## Explicitly deferred (see spec §23)

OpenSearch, Datadog, Splunk, Prometheus/Grafana, GitHub/GitLab, Jira, Slack, ServiceNow, AWS, Kubernetes, historical-incident RAG/vector search, automated remediation, runbooks, notifications, SSO, RBAC, multi-tenancy, billing.
