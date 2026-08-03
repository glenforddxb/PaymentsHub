package com.cth.sdm.handler;

import org.springframework.stereotype.Component;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;

@Component
public class DefaultDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return true;
    }

    @Override
    public String handle(File file) throws Exception {
        byte[] bytes = Files.readAllBytes(file.toPath());

        if (isBinaryFile(bytes)) {
            // Document contains binary/garbage characters. Generate clean metadata preview instead.
            StringBuilder sb = new StringBuilder();
            sb.append("====================================================\n");
            sb.append("           BINARY DOCUMENT DETECTED\n");
            sb.append("====================================================\n");
            sb.append("File Name   : ").append(file.getName()).append("\n");
            sb.append("File Size   : ").append(bytes.length).append(" bytes\n");
            sb.append("MD5 Checksum: ").append(calcMd5(bytes)).append("\n");
            sb.append("Type        : Compiled Binary/Raw Output Stream (Format Handlers Can Parse This File)\n");
            sb.append("----------------------------------------------------\n");
            sb.append("HEX DUMP PREVIEW (First 256 bytes):\n");
            sb.append(generateHexDump(bytes, 256));
            return sb.toString();
        }

        // For plain-text files, clean non-printable characters and decode cleanly as UTF-8
        String rawText = new String(bytes, StandardCharsets.UTF_8);
        return sanitizeText(rawText);
    }

    private boolean isBinaryFile(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return false;
        int checkLen = Math.min(bytes.length, 500);
        int controlChars = 0;
        for (int i = 0; i < checkLen; i++) {
            byte b = bytes[i];
            if (b == 0) {
                return true; // Null byte indicates binary format
            }
            if (b < 32 && b != 9 && b != 10 && b != 13) { // Non-whitespace control character
                controlChars++;
            }
        }
        // If more than 15% of head is control characters, classify as binary
        return ((double) controlChars / checkLen) > 0.15;
    }

    private String sanitizeText(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 32 || c == '\n' || c == '\r' || c == '\t') {
                sb.append(c);
            } else {
                sb.append(' '); // Replace non-printable ASCII control characters with spaces
            }
        }
        return sb.toString();
    }

    private String calcMd5(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    private String generateHexDump(byte[] bytes, int maxLen) {
        StringBuilder sb = new StringBuilder();
        int len = Math.min(bytes.length, maxLen);
        for (int i = 0; i < len; i += 16) {
            sb.append(String.format("%04X  ", i));
            for (int j = 0; j < 16; j++) {
                if (i + j < len) {
                    sb.append(String.format("%02X ", bytes[i + j]));
                } else {
                    sb.append("   ");
                }
            }
            sb.append(" |");
            for (int j = 0; j < 16; j++) {
                if (i + j < len) {
                    byte b = bytes[i + j];
                    char c = (b >= 32 && b < 127) ? (char) b : '.';
                    sb.append(c);
                }
            }
            sb.append("|\n");
        }
        if (bytes.length > maxLen) {
            sb.append("... [truncated to 256 bytes] ...\n");
        }
        return sb.toString();
    }
}
