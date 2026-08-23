package com.incidentiq.ai;

import java.util.List;

/**
 * Everything the AI needs to investigate an incident, gathered by the
 * investigation service before calling the provider. Deliberately a plain
 * record independent of JPA entities so the AI layer has no persistence
 * dependency.
 */
public record AIInvestigationRequest(
        String incidentTitle,
        String description,
        String service,
        String environment,
        String severity,
        String rawLogs,
        List<String> timelineHints
) {
}
