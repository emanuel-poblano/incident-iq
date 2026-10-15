package com.incidentiq.incident;

import com.incidentiq.common.exception.ResourceNotFoundException;
import com.incidentiq.incident.IncidentDtos.IncidentRequest;
import com.incidentiq.investigation.InvestigationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidents;
    private final InvestigationRepository investigations;

    public IncidentService(IncidentRepository incidents, InvestigationRepository investigations) {
        this.incidents = incidents;
        this.investigations = investigations;
    }

    @Transactional
    public Incident create(IncidentRequest req) {
        Incident i = new Incident();
        apply(i, req);
        return incidents.save(i);
    }

    @Transactional(readOnly = true)
    public List<Incident> list() {
        return incidents.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Incident get(Long id) {
        return incidents.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident " + id + " not found"));
    }

    @Transactional
    public Incident update(Long id, IncidentRequest req) {
        Incident i = get(id);
        apply(i, req);
        return i;
    }

    /** Log ingestion: appends pasted log text to the incident's evidence. */
    @Transactional
    public Incident appendLogs(Long id, String logs) {
        Incident i = incidents.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident " + id + " not found"));
        String existing = i.getRawLogs();
        i.setRawLogs(existing == null || existing.isBlank() ? logs : existing.stripTrailing() + "\n" + logs);
        return i;
    }

    @Transactional
    public void delete(Long id) {
        Incident i = get(id);
        investigations.deleteAll(investigations.findByIncidentIdOrderByCreatedAtDesc(id));
        incidents.delete(i);
    }

    private void apply(Incident i, IncidentRequest req) {
        i.setTitle(req.title());
        i.setDescription(req.description());
        i.setService(req.service());
        i.setEnvironment(req.environment());
        i.setSeverity(req.severity().toUpperCase());
        i.setRawLogs(req.rawLogs());
    }
}
