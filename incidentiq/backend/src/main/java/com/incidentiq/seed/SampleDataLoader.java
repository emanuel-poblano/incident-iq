package com.incidentiq.seed;

import com.incidentiq.incident.IncidentRepository;
import com.incidentiq.incident.IncidentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Loads the sample incidents on startup when {@code incidentiq.seed.enabled=true} and the DB is empty. */
@Component
@ConditionalOnProperty(name = "incidentiq.seed.enabled", havingValue = "true")
public class SampleDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);

    private final IncidentService incidents;
    private final IncidentRepository repository;

    public SampleDataLoader(IncidentService incidents, IncidentRepository repository) {
        this.incidents = incidents;
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        SampleIncidents.ALL.forEach(s -> incidents.create(s.toRequest()));
        log.info("Seeded {} sample incidents", SampleIncidents.ALL.size());
    }
}
