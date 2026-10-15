package com.incidentiq.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

public final class IncidentDtos {

    private IncidentDtos() {
    }

    public record IncidentRequest(
            @NotBlank String title,
            String description,
            @NotBlank String service,
            @NotBlank String environment,
            @NotBlank @Pattern(regexp = "(?i)LOW|MEDIUM|HIGH|CRITICAL", message = "must be LOW, MEDIUM, HIGH or CRITICAL")
            String severity,
            String rawLogs
    ) {
    }

    public record LogsRequest(@NotBlank String logs) {
    }

    public record IncidentResponse(
            Long id, String title, String description, String service, String environment,
            String severity, String status, String rawLogs, Instant createdAt
    ) {
        static IncidentResponse from(Incident i) {
            return new IncidentResponse(i.getId(), i.getTitle(), i.getDescription(), i.getService(),
                    i.getEnvironment(), i.getSeverity(), i.getStatus(), i.getRawLogs(), i.getCreatedAt());
        }
    }
}
