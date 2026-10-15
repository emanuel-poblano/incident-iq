CREATE TABLE incident (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    service     VARCHAR(255) NOT NULL,
    environment VARCHAR(255) NOT NULL,
    severity    VARCHAR(255) NOT NULL,
    status      VARCHAR(255) NOT NULL,
    raw_logs    TEXT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE investigation (
    id          BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incident (id),
    provider    VARCHAR(255) NOT NULL,
    summary     TEXT,
    root_cause  TEXT NOT NULL,
    confidence  DOUBLE PRECISION NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_investigation_incident ON investigation (incident_id);

CREATE TABLE investigation_evidence (
    investigation_id BIGINT NOT NULL REFERENCES investigation (id),
    type             VARCHAR(255),
    timestamp        TIMESTAMP WITH TIME ZONE,
    description      TEXT,
    relevance        TEXT
);

CREATE TABLE investigation_timeline (
    investigation_id BIGINT NOT NULL REFERENCES investigation (id),
    timestamp        TIMESTAMP WITH TIME ZONE,
    event            TEXT
);

CREATE TABLE investigation_recommendations (
    investigation_id BIGINT NOT NULL REFERENCES investigation (id),
    recommendation   TEXT
);
