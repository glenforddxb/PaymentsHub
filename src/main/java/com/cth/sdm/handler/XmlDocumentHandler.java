package com.cth.sdm.handler;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

@Component
public class XmlDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return "xml".equalsIgnoreCase(fileExtension);
    }

    @Override
    public String handle(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        StringBuilder content = new StringBuilder();
        content.append("XML Root Element: ").append(doc.getDocumentElement().getNodeName()).append("\n");

        NodeList nodeList = doc.getElementsByTagName("*");
        for (int i = 0; i < nodeList.getLength(); i++) {
            content.append(nodeList.item(i).getNodeName())
                   .append(": ")
                   .append(nodeList.item(i).getTextContent().trim())
                   .append("\n");
        }
        return content.toString();
    }
}
