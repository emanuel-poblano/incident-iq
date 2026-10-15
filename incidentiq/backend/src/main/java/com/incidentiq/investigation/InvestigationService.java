package com.incidentiq.investigation;

import com.incidentiq.ai.AIInvestigationRequest;
import com.incidentiq.ai.AIInvestigationResponse;
import com.incidentiq.ai.AIInvestigationService;
import com.incidentiq.ai.AIResponseValidator;
import com.incidentiq.common.exception.BadRequestException;
import com.incidentiq.common.exception.ResourceNotFoundException;
import com.incidentiq.incident.Incident;
import com.incidentiq.incident.IncidentRepository;
import com.incidentiq.investigation.Investigation.EvidenceRow;
import com.incidentiq.investigation.Investigation.TimelineRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orchestrates the flow: load incident + ingested logs -> analyze -> validate
 * evidence against the logs -> persist the report.
 */
@Service
public class InvestigationService {

    private final IncidentRepository incidents;
    private final InvestigationRepository investigations;
    private final AIInvestigationService analyzer;

    public InvestigationService(IncidentRepository incidents, InvestigationRepository investigations,
                                AIInvestigationService analyzer) {
        this.incidents = incidents;
        this.investigations = investigations;
        this.analyzer = analyzer;
    }

    @Transactional
    public InvestigationResponse investigate(Long incidentId) {
        Incident incident = incidents.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident " + incidentId + " not found"));
        if (incident.getRawLogs() == null || incident.getRawLogs().isBlank()) {
            throw new BadRequestException("Incident has no logs; ingest logs before investigating");
        }

        AIInvestigationResponse result = analyzer.investigate(new AIInvestigationRequest(
                incident.getTitle(), incident.getDescription(), incident.getService(),
                incident.getEnvironment(), incident.getSeverity(), incident.getRawLogs(), List.of()));
        AIResponseValidator.validate(result, incident.getRawLogs());

        Investigation inv = new Investigation();
        inv.setIncident(incident);
        inv.setProvider(analyzer.providerName());
        inv.setSummary(result.summary());
        inv.setRootCause(result.rootCause());
        inv.setConfidence(result.confidence());
        result.evidence().forEach(e ->
                inv.getEvidence().add(new EvidenceRow(e.type(), e.timestamp(), e.description(), e.relevance())));
        result.timeline().forEach(t -> inv.getTimeline().add(new TimelineRow(t.timestamp(), t.event())));
        inv.getRecommendations().addAll(result.recommendations());
        investigations.save(inv);

        incident.setStatus(Incident.INVESTIGATED);
        return InvestigationResponse.from(inv);
    }

    @Transactional(readOnly = true)
    public List<InvestigationResponse> listForIncident(Long incidentId) {
        if (!incidents.existsById(incidentId)) {
            throw new ResourceNotFoundException("Incident " + incidentId + " not found");
        }
        return investigations.findByIncidentIdOrderByCreatedAtDesc(incidentId).stream()
                .map(InvestigationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public InvestigationResponse get(Long id) {
        return investigations.findById(id).map(InvestigationResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation " + id + " not found"));
    }
}
