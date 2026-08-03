package com.cth.sdm.handler;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;

@Component
public class ExcelDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return "xlsx".equalsIgnoreCase(fileExtension) || "xls".equalsIgnoreCase(fileExtension);
    }

    @Override
    public String handle(File file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                content.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        content.append(cell.toString()).append("\t");
                    }
                    content.append("\n");
                }
            }
        }
        return content.toString();
    }
}
