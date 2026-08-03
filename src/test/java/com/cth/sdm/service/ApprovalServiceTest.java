package com.cth.sdm.service;

import com.cth.sdm.model.DocumentSubmission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ApprovalServiceTest {

    @Autowired
    private ApprovalService approvalService;

    @Test
    public void testMakerCheckerWorkflow() throws Exception {
        // Create a mock document file
        File tempFile = File.createTempFile("test-brd", ".xml");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("<root><info>Business BRD Content</info></root>");
        }

        // 1. Submit Document (Maker step)
        DocumentSubmission submission = approvalService.submitDocument("P005", "CRM", tempFile, "maker");
        assertNotNull(submission);
        assertEquals("PENDING_APPROVAL", submission.getStatus());
        assertEquals("CRM", submission.getAppCode());

        // 2. Query Pending Submissions
        List<DocumentSubmission> pending = approvalService.getPendingSubmissions();
        assertTrue(pending.stream().anyMatch(s -> s.getId().equals(submission.getId())));

        // 3. Approve Document (Checker step)
        DocumentSubmission approved = approvalService.approveDocument(submission.getId(), "checker");
        assertEquals("APPROVED", approved.getStatus());
        assertEquals("checker", approved.getCheckerUsername());

        tempFile.delete();
    }
}
