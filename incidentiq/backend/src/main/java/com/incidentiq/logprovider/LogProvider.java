package com.incidentiq.logprovider;

import java.time.Instant;
import java.util.List;

/**
 * Abstraction over "where evidence comes from". The MVP only implements a
 * PastedLogProvider (Phase 3+), operating on logs the user pasted directly
 * into the incident form. Later, OpenSearchLogProvider, DatadogLogProvider,
 * and SplunkLogProvider can implement this same interface without touching
 * the investigation service that consumes it.
 */
public interface LogProvider {

    record LogEvent(Instant timestamp, String level, String source, String message, String raw) {
    }

    List<LogEvent> search(Long incidentId);
}
