package com.cth.sdm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doc_deliverable_id")
    private DocumentDeliverable documentDeliverable;

    @Column(nullable = false, length = 3)
    private String appCode; // 3-character application code

    @Column(nullable = false)
    private String fileName;

    private String filePath;

    @Column(length = 1000000) // Support preview content storage
    private String parsedContent;

    @Builder.Default
    private String status = "PENDING_APPROVAL"; // PENDING_APPROVAL, APPROVED, REJECTED

    @Column(nullable = false)
    private String makerUsername;

    private String checkerUsername;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime approvedAt;
}
