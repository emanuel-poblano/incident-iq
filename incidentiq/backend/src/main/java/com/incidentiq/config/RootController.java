package com.incidentiq.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Phase 1 placeholder so `GET /` and `GET /api` confirm the service is up
 * before the Incident/Investigation APIs exist. Safe to remove once Feature
 * endpoints are implemented in later phases.
 */
@RestController
public class RootController {

    @GetMapping("/api")
    public Map<String, String> root() {
        return Map.of(
                "service", "incidentiq-backend",
                "status", "ok"
        );
    }
}
