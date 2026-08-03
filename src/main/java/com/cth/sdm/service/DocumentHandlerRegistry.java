package com.cth.sdm.service;

import com.cth.sdm.handler.DocumentHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentHandlerRegistry {

    private final List<DocumentHandler> handlers;

    public DocumentHandler getHandlerFor(String fileExtension) {
        return handlers.stream()
                .filter(h -> h.supports(fileExtension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No handler found for extension: " + fileExtension));
    }
}
