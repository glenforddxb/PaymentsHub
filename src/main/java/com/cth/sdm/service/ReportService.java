package com.cth.sdm.service;

import com.cth.sdm.model.DocumentSubmission;
import com.cth.sdm.repository.DocumentSubmissionRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DocumentSubmissionRepository submissionRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateExcelReport() throws Exception {
        List<DocumentSubmission> submissions = submissionRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Approvals Report");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "App Code", "Document ID", "Deliverable Name", "Status", "Maker", "Checker / Approver", "Submission Date", "Action Date"};
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Data Rows
            int rowNum = 1;
            for (DocumentSubmission sub : submissions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(sub.getId());
                row.createCell(1).setCellValue(sub.getAppCode());
                row.createCell(2).setCellValue(sub.getDocumentDeliverable() != null ? sub.getDocumentDeliverable().getDocId() : "N/A");
                row.createCell(3).setCellValue(sub.getDocumentDeliverable() != null ? sub.getDocumentDeliverable().getName() : sub.getFileName());
                row.createCell(4).setCellValue(sub.getStatus());
                row.createCell(5).setCellValue(sub.getMakerUsername());
                row.createCell(6).setCellValue(sub.getCheckerUsername() != null ? sub.getCheckerUsername() : "N/A");
                row.createCell(7).setCellValue(sub.getCreatedAt().format(formatter));
                row.createCell(8).setCellValue(sub.getApprovedAt() != null ? sub.getApprovedAt().format(formatter) : "N/A");
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] generatePdfReport() throws Exception {
        List<DocumentSubmission> submissions = submissionRepository.findAll();
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, bos);

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("Software Development Document Environment - Approvals Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Table
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100f);
        float[] columnWidths = {0.5f, 1f, 1f, 2.5f, 1.5f, 1f, 1.5f, 1.8f, 1.8f};
        table.setWidths(columnWidths);

        // Headers
        String[] headers = {"ID", "App Code", "Doc ID", "Deliverable Name", "Status", "Maker", "Checker", "Submission Date", "Action Date"};
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(h, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Rows
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (DocumentSubmission sub : submissions) {
            table.addCell(new PdfPCell(new Paragraph(String.valueOf(sub.getId()), cellFont)));
            table.addCell(new PdfPCell(new Paragraph(sub.getAppCode(), cellFont)));
            table.addCell(new PdfPCell(new Paragraph(sub.getDocumentDeliverable() != null ? sub.getDocumentDeliverable().getDocId() : "N/A", cellFont)));
            table.addCell(new PdfPCell(new Paragraph(sub.getDocumentDeliverable() != null ? sub.getDocumentDeliverable().getName() : sub.getFileName(), cellFont)));
            table.addCell(new PdfPCell(new Paragraph(sub.getStatus(), cellFont)));
            table.addCell(new PdfPCell(new Paragraph(sub.getMakerUsername(), cellFont)));
            table.addCell(new PdfPCell(new Paragraph(sub.getCheckerUsername() != null ? sub.getCheckerUsername() : "N/A", cellFont)));
            table.addCell(new PdfPCell(new Paragraph(sub.getCreatedAt().format(formatter), cellFont)));
            table.addCell(new PdfPCell(new Paragraph(sub.getApprovedAt() != null ? sub.getApprovedAt().format(formatter) : "N/A", cellFont)));
        }

        document.add(table);
        document.close();
        return bos.toByteArray();
    }
}
