package com.incidentiq.ai;

import com.incidentiq.common.exception.AIInvestigationException;

/**
 * Structural and evidence validation applied before any result is persisted.
 * Log-type evidence must quote a line that really exists in the ingested logs,
 * so a report can never cite evidence that was not observed.
 */
public final class AIResponseValidator {

    private AIResponseValidator() {
    }

    public static void validate(AIInvestigationResponse r, String rawLogs) {
        if (r == null || r.rootCause() == null || r.rootCause().isBlank()) {
            throw new AIInvestigationException("Investigation result has no root cause");
        }
        if (Double.isNaN(r.confidence()) || r.confidence() < 0 || r.confidence() > 1) {
            throw new AIInvestigationException("Confidence must be between 0 and 1");
        }
        if (r.evidence() == null || r.timeline() == null || r.recommendations() == null
                || r.recommendations().isEmpty()) {
            throw new AIInvestigationException("Investigation result is missing required sections");
        }
        if (r.confidence() > 0 && r.evidence().isEmpty()) {
            throw new AIInvestigationException("A non-zero confidence conclusion must cite evidence");
        }
        String logs = rawLogs == null ? "" : rawLogs;
        for (var e : r.evidence()) {
            if (e.description() == null || e.description().isBlank()
                    || ("LOG".equals(e.type()) || "DEPLOY_EVENT".equals(e.type())) && !logs.contains(e.description())) {
                throw new AIInvestigationException("Evidence is not present in the ingested logs");
            }
        }
    }
}
