package com.cth.sdm.service;

import com.cth.sdm.handler.DocumentHandler;
import com.cth.sdm.model.DocumentDeliverable;
import com.cth.sdm.model.DocumentSubmission;
import com.cth.sdm.repository.DocumentDeliverableRepository;
import com.cth.sdm.repository.DocumentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectoryWatcherService {

    @Value("${app.watch-folder:./ingestion-watcher}")
    private String watchFolderPath;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDirPath;

    private final DocumentHandlerRegistry handlerRegistry;
    private final DocumentSubmissionRepository submissionRepository;
    private final DocumentDeliverableRepository deliverableRepository;

    @Scheduled(fixedDelay = 5000)
    public void pollFolder() {
        File watchFolder = new File(watchFolderPath);
        if (!watchFolder.exists()) {
            boolean created = watchFolder.mkdirs();
            if (created) {
                log.info("Created watch folder at: {}", watchFolder.getAbsolutePath());
            }
            return;
        }

        File[] files = watchFolder.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Fetch first deliverable as default mapping if not specifiable, or map by naming convention
        List<DocumentDeliverable> deliverables = deliverableRepository.findAll();

        for (File file : files) {
            if (file.isFile()) {
                log.info("Detected new file for automatic ingestion: {}", file.getName());
                try {
                    String extension = getFileExtension(file.getName());
                    var handler = handlerRegistry.getHandlerFor(extension);
                    log.info("Processing document using handler: {}", handler.getClass().getSimpleName());

                    String parsedContent = handler.handle(file);
                    log.info("Document successfully parsed. Content length: {}", parsedContent.length());

                    // Move file to upload/archive directory
                    Path destination = Path.of(uploadDirPath, file.getName());
                    Files.move(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Moved processed document to: {}", destination.toAbsolutePath());

                    // Determine docId dynamically from filename prefixes (e.g., P001_xxx.xlsx) or default to first
                    String docId = "P001";
                    for (DocumentDeliverable d : deliverables) {
                        if (file.getName().toUpperCase().contains(d.getDocId())) {
                            docId = d.getDocId();
                            break;
                        }
                    }

                    DocumentDeliverable deliverable = deliverableRepository.findByDocId(docId).orElse(null);

                    // Create Submission Record
                    DocumentSubmission submission = DocumentSubmission.builder()
                            .documentDeliverable(deliverable)
                            .appCode("AUT") // AUT for Automated watched folder
                            .fileName(file.getName())
                            .filePath(destination.toAbsolutePath().toString())
                            .parsedContent(parsedContent)
                            .status("PENDING_APPROVAL")
                            .makerUsername("system-watcher")
                            .build();

                    submissionRepository.save(submission);
                    log.info("Registered automated watch-folder document under submission ID: {}", submission.getId());

                } catch (Exception e) {
                    log.error("Failed to process document {}: {}", file.getName(), e.getMessage(), e);
                }
            }
        }
    }

    private String getFileExtension(String fileName) {
        int lastIdx = fileName.lastIndexOf('.');
        if (lastIdx == -1) {
            return "";
        }
        return fileName.substring(lastIdx + 1);
    }
}
