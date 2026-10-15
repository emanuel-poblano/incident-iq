package com.incidentiq.logprovider;

import com.incidentiq.logprovider.LogProvider.LogEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses pasted log text into structured events. Understands lines like
 * {@code 2026-03-02T10:15:30Z ERROR [HikariPool-1] message} and
 * {@code 2026-03-02 10:15:30 WARN message}. Lines that do not match (stack
 * trace continuations, free text) are kept with no timestamp/level so no
 * evidence is silently dropped.
 */
public final class LogParser {

    private static final Pattern LINE = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d+)?Z?)\\s+"
                    + "(TRACE|DEBUG|INFO|WARN|WARNING|ERROR|FATAL)\\s+(?:\\[([^\\]]+)\\]\\s*)?(.*)$",
            Pattern.CASE_INSENSITIVE);

    private LogParser() {
    }

    public static List<LogEvent> parse(String rawLogs) {
        List<LogEvent> events = new ArrayList<>();
        if (rawLogs == null) {
            return events;
        }
        for (String line : rawLogs.split("\\R")) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            Matcher m = LINE.matcher(trimmed);
            if (m.matches()) {
                events.add(new LogEvent(parseTime(m.group(1)), m.group(2).toUpperCase(), m.group(3), m.group(4), trimmed));
            } else {
                events.add(new LogEvent(null, null, null, trimmed, trimmed));
            }
        }
        return events;
    }

    private static Instant parseTime(String s) {
        String t = s.replace(',', '.');
        try {
            if (t.endsWith("Z")) {
                return Instant.parse(t.replace(' ', 'T'));
            }
            return LocalDateTime.parse(t.replace(' ', 'T')).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
