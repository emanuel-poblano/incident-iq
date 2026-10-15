package com.incidentiq.incident;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "incident")
@Getter
@Setter
@NoArgsConstructor
public class Incident {

    public static final String OPEN = "OPEN";
    public static final String INVESTIGATED = "INVESTIGATED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String service;

    @Column(nullable = false)
    private String environment;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String status = OPEN;

    @Column(name = "raw_logs", columnDefinition = "TEXT")
    private String rawLogs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
