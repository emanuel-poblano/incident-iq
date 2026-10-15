package com.incidentiq.investigation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestigationRepository extends JpaRepository<Investigation, Long> {
    List<Investigation> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}
