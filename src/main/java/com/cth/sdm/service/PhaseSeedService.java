package com.cth.sdm.service;

import com.cth.sdm.model.DocumentDeliverable;
import com.cth.sdm.model.Phase;
import com.cth.sdm.repository.DocumentDeliverableRepository;
import com.cth.sdm.repository.PhaseRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhaseSeedService {

    private final PhaseRepository phaseRepository;
    private final DocumentDeliverableRepository deliverableRepository;

    @PostConstruct
    public void seedPhasesAndDeliverables() {
        if (phaseRepository.count() == 0) {
            log.info("Seeding 7 SDLC Phases and Document Deliverables...");

            Phase p1 = phaseRepository.save(new Phase(null, 1, "Prioritisation"));
            Phase p2 = phaseRepository.save(new Phase(null, 2, "Pre-project"));
            Phase p3 = phaseRepository.save(new Phase(null, 3, "Analysis"));
            Phase p4 = phaseRepository.save(new Phase(null, 4, "Design"));
            Phase p5 = phaseRepository.save(new Phase(null, 5, "Testing"));
            Phase p6 = phaseRepository.save(new Phase(null, 6, "Execution / SIT / UAT"));
            Phase p7 = phaseRepository.save(new Phase(null, 7, "Deployment"));

            addDeliverable("P001", "Project Deliverables Checklist", "Prioritisation Checklist for early SDLC filtering", "1.0", "DEL-P001", p1);
            addDeliverable("P002", "Capabilities Workshop Deck", "Presentation slide highlighting required system capabilities", "1.0", "DEL-P002", p1);
            addDeliverable("P003", "Investment Proposal", "Business investment and project budgeting proposal", "1.0", "DEL-P003", p1);
            addDeliverable("P004", "High Level Solution", "Conceptual high level solution architecture", "1.0", "DEL-P004", p1);

            addDeliverable("P005", "Business Requirement Document (BRD)", "Detailed functional and non-functional requirements", "1.0", "DEL-P005", p2);
            addDeliverable("P006", "Privacy by Design Assessment Workbook", "Security & privacy workbook mapping compliance requirements", "1.0", "DEL-P006", p2);
            addDeliverable("P007", "Data Protection Impact Assessment (DPIA) questionnaire", "DPIA workbook for information security compliance", "1.0", "DEL-P007", p2);
            addDeliverable("P008", "Information Security Risk Assessment (ISRA) Report", "Information Security Risk Assessment report details", "1.0", "DEL-P008", p2);

            addDeliverable("P009", "Functional Specification", "Detailed specifications detailing functions and processes", "1.0", "DEL-P009", p3);
            addDeliverable("P010", "Data Conversion Spec", "Data schema mapping and ETL migration strategy document", "1.0", "DEL-P010", p3);
            addDeliverable("P011", "Traceability Report", "Requirement Traceability Matrix mapping test cases to BRD", "1.0", "DEL-P011", p3);

            addDeliverable("P012", "Design Specs: System", "Detailed low-level and high-level system design documents", "1.0", "DEL-P012", p4);
            addDeliverable("P013", "Unit Test Execution Report", "Report capturing results of developer unit testing", "1.0", "DEL-P013", p4);
            addDeliverable("P014", "Source Code Review Section", "Summary code review reports (SAST & SCA)", "1.0", "DEL-P014", p4);

            addDeliverable("P015", "Test and Defect Status Report", "Daily/weekly report tracking active QA defect lifecycle", "1.0", "DEL-P015", p5);
            addDeliverable("P016", "Performance Test Status Report", "Load testing, stress testing, and scalability status logs", "1.0", "DEL-P016", p5);
            addDeliverable("P017", "Performance Test Summary Report", "Consolidated performance test outcomes and resource usage", "1.0", "DEL-P017", p5);

            addDeliverable("P018", "SIT Traceability Report", "System Integration Testing traceability matrices", "1.0", "DEL-P018", p6);
            addDeliverable("P019", "Statement of Acceptance (SIT)", "Formal approval declaring SIT execution completion", "1.0", "DEL-P019", p6);
            addDeliverable("P020", "UAT Traceability Report", "User Acceptance Testing traceability mapping and user stories", "1.0", "DEL-P020", p6);
            addDeliverable("P021", "Statement of Acceptance (UAT)", "Formal business stakeholder UAT acceptance signoff", "1.0", "DEL-P021", p6);

            addDeliverable("P022", "Deployment Handover to Support/Operations", "Standard support team handbook and contact escalation", "1.0", "DEL-P022", p7);
            addDeliverable("P023", "Deployment Release Notes", "Features, updates, and configuration parameters deployed in current release", "1.0", "DEL-P023", p7);
            addDeliverable("P024", "DR Plan", "Disaster Recovery strategies, backup parameters, and restore drills", "1.0", "DEL-P024", p7);
            addDeliverable("P025", "Implementation Plan", "Production implementation step-by-step checklist and timeline", "1.0", "DEL-P025", p7);

            log.info("7 SDLC Phases and Deliverables seeded successfully.");
        }
    }

    private void addDeliverable(String docId, String name, String desc, String version, String code, Phase phase) {
        DocumentDeliverable deliverable = DocumentDeliverable.builder()
                .docId(docId)
                .name(name)
                .description(desc)
                .version(version)
                .docCode(code)
                .phase(phase)
                .build();
        deliverableRepository.save(deliverable);
    }
}
