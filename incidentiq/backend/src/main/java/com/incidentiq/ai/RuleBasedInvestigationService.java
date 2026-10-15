package com.incidentiq.ai;

import com.incidentiq.ai.AIInvestigationResponse.EvidenceItem;
import com.incidentiq.ai.AIInvestigationResponse.TimelineEntry;
import com.incidentiq.logprovider.LogParser;
import com.incidentiq.logprovider.LogProvider.LogEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Deterministic, offline investigation engine. It matches known failure
 * signatures against the ingested logs, and every conclusion cites the exact
 * log lines that triggered it. When nothing matches it reports zero confidence
 * instead of guessing. Needs no API key, so tests and demos run anywhere; an
 * LLM-backed implementation can replace it behind {@link AIInvestigationService}.
 */
@Service
public class RuleBasedInvestigationService implements AIInvestigationService {

    static final String PROVIDER = "rule-based";
    private static final int MAX_EVIDENCE = 6;

    private record Rule(String name, String rootCause, double baseConfidence, Pattern signal,
                        Pattern context, String relevance, List<String> recommendations) {
        Rule(String name, String rootCause, double base, String signal, String context, String relevance,
             List<String> recs) {
            this(name, rootCause, base, Pattern.compile(signal, Pattern.CASE_INSENSITIVE),
                    context == null ? null : Pattern.compile(context, Pattern.CASE_INSENSITIVE), relevance, recs);
        }
    }

    private static final List<Rule> RULES = List.of(
            new Rule("db-pool", "Database connection pool exhaustion", 0.7,
                    "connection is not available|pool.*(exhausted|timeout)|timeout waiting for (a )?connection|too many connections|active=\\d+ idle=0",
                    null,
                    "Shows the application could not obtain a database connection from the pool",
                    List.of("Check for leaked connections or long-running queries holding connections",
                            "Review pool size against database max_connections",
                            "Add connection-leak detection and alerting on pool utilisation")),
            new Rule("memory", "Memory leak leading to heap exhaustion (OutOfMemoryError)", 0.7,
                    "OutOfMemoryError|java heap space|GC overhead limit|OOMKilled|heap usage (at )?9\\d%",
                    null,
                    "Shows memory pressure and heap exhaustion before the crash",
                    List.of("Capture and analyse a heap dump to find the retained objects",
                            "Restart affected instances to restore service while the leak is fixed",
                            "Add heap usage alerting and set explicit memory limits")),
            new Rule("tls", "Expired TLS certificate", 0.75,
                    "certificate.*expired|CertificateExpiredException",
                    null,
                    "Shows TLS handshakes failing because a certificate is expired",
                    List.of("Renew and deploy the expired certificate immediately",
                            "Automate certificate renewal (e.g. ACME)",
                            "Add certificate expiry monitoring with 30/14/7 day alerts")),
            new Rule("dependency-timeout", "External dependency timeout (upstream service not responding)", 0.65,
                    "read timed out|connect timed out|SocketTimeoutException|upstream.*(timeout|timed out)|504 gateway|circuit breaker.*open",
                    null,
                    "Shows calls to an external dependency timing out",
                    List.of("Check the dependency's status page and contact its provider",
                            "Tune client timeouts, retries with backoff and circuit breakers",
                            "Provide a graceful degradation path while the dependency is down")),
            new Rule("bad-deploy", "Faulty deployment introducing application errors (HTTP 500s)", 0.6,
                    "NullPointerException|NoSuchMethodError|ClassNotFoundException|status=500|HTTP 500|Unhandled exception",
                    "deploy(ed|ing|ment)?\\b|rollout|release v?\\d|version \\S+ (started|live)",
                    "Shows application errors, correlated with a recent deployment",
                    List.of("Roll back to the last known good release",
                            "Review the diff of the latest deployment for the failing code path",
                            "Add canary deployments and automated rollback on error-rate increase"))
    );

    @Override
    public String providerName() {
        return PROVIDER;
    }

    @Override
    public AIInvestigationResponse investigate(AIInvestigationRequest request) {
        List<LogEvent> events = LogParser.parse(request.rawLogs());
        Rule match = null;
        List<LogEvent> matchHits = List.of();
        for (Rule rule : RULES) {
            if (rule.context() != null
                    && events.stream().noneMatch(e -> rule.context().matcher(e.raw()).find())) {
                continue;
            }
            List<LogEvent> hits = events.stream()
                    .filter(e -> isProblem(e) && rule.signal().matcher(e.raw()).find()).toList();
            if (hits.size() > matchHits.size()) {
                match = rule;
                matchHits = hits;
            }
        }
        final Rule best = match;
        final List<LogEvent> bestHits = matchHits;
        if (best == null) {
            return new AIInvestigationResponse(
                    "No known failure signature was found in the " + events.size() + " ingested log lines.",
                    "Insufficient evidence to determine a root cause", 0.0, List.of(), List.of(),
                    List.of("Ingest more logs covering the incident window (errors, warnings, deploy events)",
                            "Include logs from dependent services and infrastructure"));
        }

        List<LogEvent> context = new ArrayList<>();
        if (best.context() != null) {
            events.stream().filter(e -> best.context().matcher(e.raw()).find() && !bestHits.contains(e))
                    .limit(2).forEach(context::add);
        }
        double confidence = Math.min(0.95, best.baseConfidence() + 0.05 * Math.min(bestHits.size() - 1, 4)
                + (context.isEmpty() ? 0 : 0.1));

        List<EvidenceItem> evidence = new ArrayList<>();
        for (LogEvent e : context) {
            evidence.add(new EvidenceItem("DEPLOY_EVENT", e.timestamp(), e.raw(),
                    "Change event in the incident window that may have triggered the failure"));
        }
        for (LogEvent e : bestHits.stream().limit(MAX_EVIDENCE).toList()) {
            evidence.add(new EvidenceItem("LOG", e.timestamp(), e.raw(), best.relevance()));
        }

        List<TimelineEntry> timeline = evidence.stream().filter(e -> e.timestamp() != null)
                .map(e -> new TimelineEntry(e.timestamp(), e.description()))
                .sorted(Comparator.comparing(TimelineEntry::timestamp)).toList();

        String summary = String.format("%d of %d log lines match the '%s' failure signature on %s (%s).",
                bestHits.size(), events.size(), best.name(), request.service(), request.environment());
        return new AIInvestigationResponse(summary, best.rootCause(), confidence, evidence, timeline,
                best.recommendations());
    }

    private static boolean isProblem(LogEvent e) {
        return e.level() == null || !(e.level().equals("INFO") || e.level().equals("DEBUG") || e.level().equals("TRACE"))
                || e.raw().matches("(?i).*\\b(exhausted|timeout|timed out|expired|OutOfMemory)\\b.*");
    }
}
