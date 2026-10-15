package com.incidentiq.investigation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InvestigationController {

    private final InvestigationService service;

    public InvestigationController(InvestigationService service) {
        this.service = service;
    }

    @PostMapping("/incidents/{id}/investigate")
    @ResponseStatus(HttpStatus.CREATED)
    public InvestigationResponse investigate(@PathVariable Long id) {
        return service.investigate(id);
    }

    @GetMapping("/incidents/{id}/investigations")
    public List<InvestigationResponse> history(@PathVariable Long id) {
        return service.listForIncident(id);
    }

    @GetMapping("/investigations/{id}")
    public InvestigationResponse get(@PathVariable Long id) {
        return service.get(id);
    }
}
