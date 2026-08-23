package com.incidentiq.ai;

import java.time.Instant;
import java.util.List;

/**
 * Structured investigation result. The application never parses free-form AI
 * text — providers must return (or be coerced into) this exact shape, which
 * is then validated by AIResponseValidator before anything is persisted.
 */
public record AIInvestigationResponse(
        String summary,
        String rootCause,
        double confidence,
        List<EvidenceItem> evidence,
        List<TimelineEntry> timeline,
        List<String> recommendations
) {
    public record EvidenceItem(
            String type,
            Instant timestamp,
            String description,
            String relevance
    ) {
    }

    public record TimelineEntry(
            Instant timestamp,
            String event
    ) {
    }
}
