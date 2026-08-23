package com.incidentiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * IncidentIQ backend entry point.
 *
 * IncidentIQ is an AI-powered incident investigation platform. It sits on top
 * of existing engineering data (logs, evidence) and produces an
 * evidence-backed probable root cause, confidence score, timeline, and
 * recommended next steps for a given incident.
 */
@SpringBootApplication
public class IncidentIqApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentIqApplication.class, args);
    }
}
