package com.incidentiq.seed;

import com.incidentiq.incident.IncidentDtos.IncidentRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Five realistic demo incidents, each with logs and the root cause the investigation should find. */
public final class SampleIncidents {

    public record Sample(String logFile, String title, String description, String service,
                         String environment, String severity, String expectedRootCause) {
        public IncidentRequest toRequest() {
            return new IncidentRequest(title, description, service, environment, severity, readLogs());
        }

        public String readLogs() {
            try (InputStream in = SampleIncidents.class.getResourceAsStream("/samples/" + logFile)) {
                if (in == null) {
                    throw new IllegalStateException("Missing sample log " + logFile);
                }
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public static final List<Sample> ALL = List.of(
            new Sample("db-pool-exhaustion.log", "Order API returning 503s",
                    "Customers cannot load orders; requests hang then fail.", "order-service", "production",
                    "CRITICAL", "Database connection pool exhaustion"),
            new Sample("payment-api-timeout.log", "Checkout payments failing",
                    "Checkout fails at the payment step for most carts.", "checkout-service", "production",
                    "HIGH", "External dependency timeout"),
            new Sample("memory-leak-crash.log", "Report service restarting repeatedly",
                    "Pods restart every few hours with exit code 137.", "report-service", "production",
                    "HIGH", "Memory leak"),
            new Sample("bad-deployment-500s.log", "Profile page errors after release",
                    "HTTP 500s began right after the latest deployment.", "profile-service", "production",
                    "CRITICAL", "Faulty deployment"),
            new Sample("expired-tls-certificate.log", "Partner integration handshake failures",
                    "All calls to the partner API fail at midnight.", "gateway", "production",
                    "HIGH", "Expired TLS certificate")
    );

    private SampleIncidents() {
    }
}
