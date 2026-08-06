package com.campusos.controller;

import com.campusos.service.PdfService;
import com.campusos.service.StudyBuzzService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/studybuzz")
public class StudyBuzzController {

    private final StudyBuzzService studyBuzzService;
    private final PdfService pdfService;

    public StudyBuzzController(StudyBuzzService studyBuzzService, PdfService pdfService) {
        this.studyBuzzService = studyBuzzService;
        this.pdfService = pdfService;
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body, org.springframework.security.core.Authentication auth) {
        String message = (String) body.get("message");
        Map<String, String> context = (Map<String, String>) body.getOrDefault("context", Map.of());
        String userId = null;
        if (auth != null && auth.getPrincipal() instanceof com.campusos.model.UserEntity) {
            userId = ((com.campusos.model.UserEntity) auth.getPrincipal()).getId().toString();
        }
        return ResponseEntity.ok(studyBuzzService.chatResponse(userId, message, context));
    }

    @PostMapping("/pdf-summarize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> pdfSummarize(@RequestBody Map<String, String> body, org.springframework.security.core.Authentication auth) {
        String url = body.get("url");
        String userId = null;
        if (auth != null && auth.getPrincipal() instanceof com.campusos.model.UserEntity) {
            userId = ((com.campusos.model.UserEntity) auth.getPrincipal()).getId().toString();
        }
        try {
            String text = pdfService.fetchAndExtractText(url);
            return ResponseEntity.ok(studyBuzzService.summarizePdf(userId, text));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }
}
