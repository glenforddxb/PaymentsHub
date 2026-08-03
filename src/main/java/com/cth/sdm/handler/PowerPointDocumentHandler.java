package com.cth.sdm.handler;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;

@Component
public class PowerPointDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return "pptx".equalsIgnoreCase(fileExtension) || "ppt".equalsIgnoreCase(fileExtension);
    }

    @Override
    public String handle(File file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {
            int slideIndex = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                content.append("Slide ").append(slideIndex++).append(":\n");
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        content.append(textShape.getText()).append("\n");
                    }
                }
            }
        }
        return content.toString();
    }
}
