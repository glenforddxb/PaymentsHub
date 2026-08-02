package com.cth.sdm.handler;

import org.springframework.stereotype.Component;
import java.io.File;
import java.nio.file.Files;

@Component
public class DefaultDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return true;
    }

    @Override
    public String handle(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()));
    }
}
