package com.incidentiq.investigation;

import java.time.Instant;
import java.util.List;

/** The saved investigation report returned by the API. */
public record InvestigationResponse(
        Long id,
        Long incidentId,
        String provider,
        String summary,
        String rootCause,
        double confidence,
        List<Evidence> evidence,
        List<TimelineEvent> timeline,
        List<String> recommendations,
        Instant createdAt
) {
    public record Evidence(String type, Instant timestamp, String description, String relevance) {
    }

    public record TimelineEvent(Instant timestamp, String event) {
    }

    static InvestigationResponse from(Investigation i) {
        return new InvestigationResponse(
                i.getId(), i.getIncident().getId(), i.getProvider(), i.getSummary(), i.getRootCause(),
                i.getConfidence(),
                i.getEvidence().stream()
                        .map(e -> new Evidence(e.getType(), e.getTimestamp(), e.getDescription(), e.getRelevance()))
                        .toList(),
                i.getTimeline().stream().map(t -> new TimelineEvent(t.getTimestamp(), t.getEvent())).toList(),
                List.copyOf(i.getRecommendations()),
                i.getCreatedAt());
    }
}
