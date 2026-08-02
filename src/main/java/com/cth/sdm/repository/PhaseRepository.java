package com.cth.sdm.repository;

import com.cth.sdm.model.Phase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PhaseRepository extends JpaRepository<Phase, Long> {
    Optional<Phase> findByPhaseNumber(Integer phaseNumber);
}
