package com.incidentiq.investigation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentiq.ai.AIInvestigationResponse;
import com.incidentiq.ai.AIResponseValidator;
import com.incidentiq.common.exception.AIInvestigationException;
import com.incidentiq.logprovider.LogParser;
import com.incidentiq.seed.SampleIncidents;
import com.incidentiq.seed.SampleIncidents.Sample;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InvestigationFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    static Stream<Sample> samples() {
        return SampleIncidents.ALL.stream();
    }

    private JsonNode call(org.springframework.test.web.servlet.RequestBuilder rb, int expected) throws Exception {
        MvcResult r = mvc.perform(rb).andExpect(status().is(expected)).andReturn();
        String body = r.getResponse().getContentAsString();
        return body.isEmpty() ? null : json.readTree(body);
    }

    private JsonNode postJson(String url, Object body, int expected) throws Exception {
        return call(post(url).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)), expected);
    }

    @ParameterizedTest
    @MethodSource("samples")
    void fullFlowForEverySampleIncident(Sample sample) throws Exception {
        // create incident without logs, then ingest them
        Map<String, Object> req = Map.of("title", sample.title(), "service", sample.service(),
                "environment", sample.environment(), "severity", sample.severity());
        JsonNode incident = postJson("/api/incidents", req, 201);
        long id = incident.get("id").asLong();
        postJson("/api/incidents/" + id + "/logs", Map.of("logs", sample.readLogs()), 200);

        // investigate
        JsonNode report = postJson("/api/incidents/" + id + "/investigate", Map.of(), 201);
        assertThat(report.get("rootCause").asText()).containsIgnoringCase(sample.expectedRootCause());
        assertThat(report.get("confidence").asDouble()).isBetween(0.5, 1.0);
        assertThat(report.get("evidence")).isNotEmpty();
        assertThat(report.get("timeline")).isNotEmpty();
        assertThat(report.get("recommendations")).isNotEmpty();
        String logs = sample.readLogs();
        report.get("evidence").forEach(e -> assertThat(logs).contains(e.get("description").asText()));

        // saved report is retrievable and incident status updated
        JsonNode saved = call(get("/api/investigations/" + report.get("id").asLong()), 200);
        assertThat(saved.get("rootCause")).isEqualTo(report.get("rootCause"));
        assertThat(call(get("/api/incidents/" + id + "/investigations"), 200)).hasSize(1);
        assertThat(call(get("/api/incidents/" + id), 200).get("status").asText()).isEqualTo("INVESTIGATED");
    }

    @Test
    void unknownLogsYieldZeroConfidenceNotAGuess() throws Exception {
        JsonNode incident = postJson("/api/incidents", Map.of("title", "t", "service", "s", "environment", "e",
                "severity", "low", "rawLogs", "2026-01-01T00:00:00Z INFO [app] all good\n2026-01-01T00:00:01Z INFO [app] still good"), 201);
        JsonNode report = postJson("/api/incidents/" + incident.get("id").asLong() + "/investigate", Map.of(), 201);
        assertThat(report.get("confidence").asDouble()).isZero();
        assertThat(report.get("evidence")).isEmpty();
    }

    @Test
    void investigatingWithoutLogsIsRejected() throws Exception {
        JsonNode incident = postJson("/api/incidents", Map.of("title", "t", "service", "s", "environment", "e",
                "severity", "LOW"), 201);
        postJson("/api/incidents/" + incident.get("id").asLong() + "/investigate", Map.of(), 400);
    }

    @Test
    void validationAndNotFound() throws Exception {
        postJson("/api/incidents", Map.of("title", ""), 400);
        call(get("/api/incidents/999999"), 404);
        postJson("/api/incidents/999999/investigate", Map.of(), 404);
        call(get("/api/investigations/999999"), 404);
    }

    @Test
    void deleteRemovesIncidentAndItsInvestigations() throws Exception {
        Sample s = SampleIncidents.ALL.get(0);
        long id = postJson("/api/incidents", s.toRequest(), 201).get("id").asLong();
        postJson("/api/incidents/" + id + "/investigate", Map.of(), 201);
        call(delete("/api/incidents/" + id), 204);
        call(get("/api/incidents/" + id), 404);
    }

    @Test
    void parserHandlesStructuredAndUnstructuredLines() {
        var events = LogParser.parse("2026-03-02 10:15:30 WARN [src] hello\n  at com.Foo.bar(Foo.java:1)\n\n");
        assertThat(events).hasSize(2);
        assertThat(events.get(0).level()).isEqualTo("WARN");
        assertThat(events.get(0).source()).isEqualTo("src");
        assertThat(events.get(0).timestamp()).isNotNull();
        assertThat(events.get(1).timestamp()).isNull();
    }

    @Test
    void validatorRejectsEvidenceNotInLogs() {
        var fake = new AIInvestigationResponse("s", "cause", 0.9,
                List.of(new AIInvestigationResponse.EvidenceItem("LOG", null, "invented line", "r")),
                List.of(), List.of("do something"));
        assertThatThrownBy(() -> AIResponseValidator.validate(fake, "real line"))
                .isInstanceOf(AIInvestigationException.class);
    }
}
