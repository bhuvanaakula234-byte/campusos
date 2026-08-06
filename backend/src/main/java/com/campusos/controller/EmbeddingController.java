package com.campusos.controller;

import com.campusos.service.EmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    public ResponseEntity<?> create(@RequestBody Map<String,String> body) {
        String content = body.get("content");
        String metadata = body.getOrDefault("metadata", "");
        return ResponseEntity.ok(embeddingService.create(content, metadata));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q, @RequestParam(defaultValue = "5") int k) {
        return ResponseEntity.ok(embeddingService.retrieve(q, k));
    }
}
