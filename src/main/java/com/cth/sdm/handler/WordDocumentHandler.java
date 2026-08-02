package com.cth.sdm.handler;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Component
public class WordDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return "docx".equalsIgnoreCase(fileExtension) || "doc".equalsIgnoreCase(fileExtension);
    }

    @Override
    public String handle(File file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                content.append(para.getText()).append("\n");
            }
        }
        return content.toString();
    }
}
