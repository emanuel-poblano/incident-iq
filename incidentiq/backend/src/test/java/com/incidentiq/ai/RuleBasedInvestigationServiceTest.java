package com.incidentiq.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedInvestigationServiceTest {

    private final RuleBasedInvestigationService service = new RuleBasedInvestigationService();

    @Test
    void ambiguousCertificateValidationFailuresDoNotDiagnoseExpiry() {
        var response = investigate("""
                ERROR PKIX path validation failed
                ERROR certificate is not valid yet; notAfter=2026-12-01
                """);

        assertThat(response.rootCause()).contains("Insufficient evidence");
        assertThat(response.recommendations()).noneMatch(r -> r.contains("Renew"));
    }

    @Test
    void applicationErrorsNeedDeploymentContextForFaultyDeploymentDiagnosis() {
        var response = investigate("ERROR NullPointerException status=500");

        assertThat(response.rootCause()).contains("Insufficient evidence");
        assertThat(response.recommendations()).noneMatch(r -> r.contains("Roll back"));
    }

    @Test
    void explicitExpiryAndDeploymentContextStillProduceSpecificDiagnoses() {
        var expiredCertificate = investigate("ERROR certificate for partner-api has expired");
        var faultyDeployment = investigate("""
                INFO Deployment of profile-service version 2.14.0 started
                ERROR Request completed with status=500
                """);

        assertThat(expiredCertificate.rootCause()).contains("Expired TLS certificate");
        assertThat(faultyDeployment.rootCause()).contains("Faulty deployment");
    }

    private AIInvestigationResponse investigate(String logs) {
        return service.investigate(new AIInvestigationRequest(
                "title", null, "service", "production", "HIGH", logs, List.of()));
    }
}
