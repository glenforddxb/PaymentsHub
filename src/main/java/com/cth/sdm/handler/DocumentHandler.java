package com.cth.sdm.handler;

import java.io.File;

public interface DocumentHandler {
    boolean supports(String fileExtension);
    String handle(File file) throws Exception;
}
