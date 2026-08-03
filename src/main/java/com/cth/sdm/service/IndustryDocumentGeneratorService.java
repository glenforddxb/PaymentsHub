package com.cth.sdm.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;

@Service
@Slf4j
public class IndustryDocumentGeneratorService {

    @PostConstruct
    public void generateIndustryDocuments() {
        File dir = new File("src/main/resources/templates/docs");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // 1. Generate Word Document: Functional Specification
            File wordFile = new File(dir, "Functional_Specification.docx");
            if (!wordFile.exists()) {
                try (XWPFDocument document = new XWPFDocument();
                     FileOutputStream out = new FileOutputStream(wordFile)) {
                    XWPFParagraph title = document.createParagraph();
                    XWPFRun titleRun = title.createRun();
                    titleRun.setText("Functional Specification Document");
                    titleRun.setBold(true);
                    titleRun.setFontSize(18);

                    XWPFParagraph body = document.createParagraph();
                    XWPFRun bodyRun = body.createRun();
                    bodyRun.setText("\nThis document defines the Functional Specifications of the Software Development Document Environment (SDM). "
                            + "It handles Multi-Phase Deliverable uploads, automatic folder watches, Maker-Checker validation workflows, and dynamic PDF/Excel reporting.");
                }
                log.info("Generated Industry Deliverable Word Document: {}", wordFile.getName());
            }

            // 2. Generate Excel Workbook: Walkthrough Guide
            File excelFile = new File(dir, "Walkthrough_Guide.xlsx");
            if (!excelFile.exists()) {
                try (Workbook workbook = new XSSFWorkbook();
                     FileOutputStream out = new FileOutputStream(excelFile)) {
                    Sheet sheet = workbook.createSheet("Walkthrough Guide");
                    Row headerRow = sheet.createRow(0);
                    headerRow.createCell(0).setCellValue("Step");
                    headerRow.createCell(1).setCellValue("Description");

                    Row row1 = sheet.createRow(1);
                    row1.createCell(0).setCellValue("1. Ingestion");
                    row1.createCell(1).setCellValue("System directory watcher polls for incoming XML, Excel, Word, or PowerPoint documents.");

                    Row row2 = sheet.createRow(2);
                    row2.createCell(0).setCellValue("2. Verification");
                    row2.createCell(1).setCellValue("Maker uploads a deliverable matching one of the 7 SDLC phases. System transitions state to PENDING_APPROVAL.");

                    Row row3 = sheet.createRow(3);
                    row3.createCell(0).setCellValue("3. Approval");
                    row3.createCell(1).setCellValue("Checker reviews the parsed content extraction and either approves or rejects the registration.");

                    workbook.write(out);
                }
                log.info("Generated Industry Deliverable Excel Workbook: {}", excelFile.getName());
            }

            // 3. Generate PowerPoint Presentation: Instruction Deck
            File pptFile = new File(dir, "Instruction_Deck.pptx");
            if (!pptFile.exists()) {
                try (XMLSlideShow ppt = new XMLSlideShow();
                     FileOutputStream out = new FileOutputStream(pptFile)) {
                    XSLFSlide slide = ppt.createSlide();
                    XSLFTextBox titleBox = slide.createTextBox();
                    titleBox.setText("Software Development Document Environment");
                    log.info("Generating PPT Deliverable slide deck...");
                    ppt.write(out);
                }
                log.info("Generated Industry Deliverable PPTX Slideshow: {}", pptFile.getName());
            }

        } catch (Exception e) {
            log.error("Failed to generate program-level walkthrough, specs or instruction guides: {}", e.getMessage(), e);
        }
    }
}
