package com.incidentiq.investigation;

import com.incidentiq.incident.Incident;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "investigation")
@Getter
@Setter
@NoArgsConstructor
public class Investigation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @Column(nullable = false)
    private String provider;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "root_cause", nullable = false, columnDefinition = "TEXT")
    private String rootCause;

    @Column(nullable = false)
    private double confidence;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @ElementCollection
    @CollectionTable(name = "investigation_evidence", joinColumns = @JoinColumn(name = "investigation_id"))
    private List<EvidenceRow> evidence = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "investigation_timeline", joinColumns = @JoinColumn(name = "investigation_id"))
    private List<TimelineRow> timeline = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "investigation_recommendations", joinColumns = @JoinColumn(name = "investigation_id"))
    @Column(name = "recommendation", columnDefinition = "TEXT")
    private List<String> recommendations = new ArrayList<>();

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenceRow {
        private String type;
        private Instant timestamp;
        @Column(columnDefinition = "TEXT")
        private String description;
        @Column(columnDefinition = "TEXT")
        private String relevance;
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineRow {
        private Instant timestamp;
        @Column(columnDefinition = "TEXT")
        private String event;
    }
}
