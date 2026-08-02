package com.cth.sdm.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "phases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer phaseNumber;

    @Column(nullable = false)
    private String name;
}
