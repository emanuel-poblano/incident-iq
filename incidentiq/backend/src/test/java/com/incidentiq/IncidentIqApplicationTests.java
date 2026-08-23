package com.incidentiq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Baseline smoke test: the Spring context must load cleanly. Uses the H2
 * in-memory database (see application-test.yml) so the suite never requires
 * a real PostgreSQL instance or AI API key, per the testing requirements.
 */
@SpringBootTest
@ActiveProfiles("test")
class IncidentIqApplicationTests {

    @Test
    void contextLoads() {
    }
}
