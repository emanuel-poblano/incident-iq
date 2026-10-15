package com.incidentiq.logprovider;

import com.incidentiq.common.exception.ResourceNotFoundException;
import com.incidentiq.incident.IncidentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** MVP log provider: evidence is the log text pasted/ingested on the incident itself. */
@Component
public class PastedLogProvider implements LogProvider {

    private final IncidentRepository incidents;

    public PastedLogProvider(IncidentRepository incidents) {
        this.incidents = incidents;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogEvent> search(Long incidentId) {
        return LogParser.parse(incidents.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident " + incidentId + " not found"))
                .getRawLogs());
    }
}
