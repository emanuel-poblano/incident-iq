package com.incidentiq.incident;

import com.incidentiq.incident.IncidentDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService service;

    public IncidentController(IncidentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse create(@Valid @RequestBody IncidentRequest req) {
        return IncidentResponse.from(service.create(req));
    }

    @GetMapping
    public List<IncidentResponse> list() {
        return service.list().stream().map(IncidentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public IncidentResponse get(@PathVariable Long id) {
        return IncidentResponse.from(service.get(id));
    }

    @PutMapping("/{id}")
    public IncidentResponse update(@PathVariable Long id, @Valid @RequestBody IncidentRequest req) {
        return IncidentResponse.from(service.update(id, req));
    }

    @PostMapping("/{id}/logs")
    public IncidentResponse ingestLogs(@PathVariable Long id, @Valid @RequestBody LogsRequest req) {
        return IncidentResponse.from(service.appendLogs(id, req.logs()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
