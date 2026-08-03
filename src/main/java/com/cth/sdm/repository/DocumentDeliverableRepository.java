package com.cth.sdm.repository;

import com.cth.sdm.model.DocumentDeliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentDeliverableRepository extends JpaRepository<DocumentDeliverable, Long> {
    Optional<DocumentDeliverable> findByDocId(String docId);
}
