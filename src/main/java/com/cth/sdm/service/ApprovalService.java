package com.cth.sdm.service;

import com.cth.sdm.model.DocumentDeliverable;
import com.cth.sdm.model.DocumentSubmission;
import com.cth.sdm.repository.DocumentDeliverableRepository;
import com.cth.sdm.repository.DocumentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final DocumentSubmissionRepository submissionRepository;
    private final DocumentDeliverableRepository deliverableRepository;
    private final DocumentHandlerRegistry handlerRegistry;

    private boolean notificationsEnabled = false;

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
        log.info("Maker-Checker toggle for Approver Notifications updated to: {}", notificationsEnabled);
    }

    public DocumentSubmission submitDocument(String docId, String appCode, File file, String makerUsername) throws Exception {
        DocumentDeliverable deliverable = deliverableRepository.findByDocId(docId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Document ID: " + docId));

        if (appCode == null || appCode.length() != 3) {
            throw new IllegalArgumentException("Application Code must be exactly 3 characters.");
        }

        String extension = getFileExtension(file.getName());
        var handler = handlerRegistry.getHandlerFor(extension);
        String content = handler.handle(file);

        DocumentSubmission submission = DocumentSubmission.builder()
                .documentDeliverable(deliverable)
                .appCode(appCode.toUpperCase())
                .fileName(file.getName())
                .filePath(file.getAbsolutePath())
                .parsedContent(content)
                .status("PENDING_APPROVAL")
                .makerUsername(makerUsername)
                .build();

        DocumentSubmission saved = submissionRepository.save(submission);
        log.info("Maker '{}' submitted document '{}' for app code '{}'. Saved submission ID: {}",
                makerUsername, file.getName(), appCode, saved.getId());

        triggerApproverNotification(saved);
        return saved;
    }

    public DocumentSubmission approveDocument(Long submissionId, String checkerUsername) {
        DocumentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        submission.setStatus("APPROVED");
        submission.setCheckerUsername(checkerUsername);
        submission.setApprovedAt(LocalDateTime.now());

        log.info("Checker '{}' APPROVED submission ID: {}", checkerUsername, submissionId);
        return submissionRepository.save(submission);
    }

    public DocumentSubmission rejectDocument(Long submissionId, String checkerUsername) {
        DocumentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        submission.setStatus("REJECTED");
        submission.setCheckerUsername(checkerUsername);
        submission.setApprovedAt(LocalDateTime.now());

        log.info("Checker '{}' REJECTED submission ID: {}", checkerUsername, submissionId);
        return submissionRepository.save(submission);
    }

    public List<DocumentSubmission> getPendingSubmissions() {
        return submissionRepository.findByStatus("PENDING_APPROVAL");
    }

    public List<DocumentSubmission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    private void triggerApproverNotification(DocumentSubmission submission) {
        if (!notificationsEnabled) {
            log.info("Approver notification toggle is disabled. Skipping SMS/Email alert for submission ID: {}", submission.getId());
            return;
        }
        log.info("--- NOTIFICATION TRIGGERED ---");
        log.info("Sending Email/SMS notification to Approvers: New document '{}' has been submitted for approval by '{}'",
                submission.getFileName(), submission.getMakerUsername());
        log.info("------------------------------");
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx == -1 ? "" : fileName.substring(idx + 1);
    }
}
