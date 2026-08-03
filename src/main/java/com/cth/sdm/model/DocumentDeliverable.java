package com.cth.sdm.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_deliverables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDeliverable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String docId; // e.g., P001, P002

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Builder.Default
    private String version = "1.0";

    private String docCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phase_id", nullable = false)
    private Phase phase;
}
