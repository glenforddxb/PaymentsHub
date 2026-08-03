package com.cth.sdm.repository;

import com.cth.sdm.model.DocumentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentSubmissionRepository extends JpaRepository<DocumentSubmission, Long> {
    List<DocumentSubmission> findByStatus(String status);
    List<DocumentSubmission> findByAppCode(String appCode);
}
