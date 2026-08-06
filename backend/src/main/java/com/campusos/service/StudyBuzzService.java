package com.campusos.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class StudyBuzzService {

    private final OpenAIClient openAIClient;
    private final EmbeddingService embeddingService;

    public StudyBuzzService(OpenAIClient openAIClient, EmbeddingService embeddingService) {
        this.openAIClient = openAIClient;
        this.embeddingService = embeddingService;
    }

    // Chat response uses OpenAI when available, else falls back to prototype. Uses RAG when possible.
    public Map<String, Object> chatResponse(String userId, String message, Map<String, String> context) {
        Map<String, Object> r = new HashMap<>();
        if (message == null || message.isBlank()) {
            r.put("reply", "Please ask a question about your syllabus, subjects, or study plan.");
            r.put("type", "prompt");
            return r;
        }

        // If embeddings available and DB has content, do a retrieval first
        try {
            var retrieved = embeddingService.retrieve(message, 5);
            StringBuilder ctx = new StringBuilder();
            for (var item : retrieved) {
                double score = (Double) item.get("score");
                if (score > 0.0) {
                    ctx.append("Source (score=").append(String.format("%.3f", score)).append("): \n");
                    ctx.append((String) item.get("content")).append("\n\n");
                }
            }
            String prompt = "You are StudyBuzz, an assistant for students. Use the retrieved documents as context and answer the question.\n" +
                    "CONTEXT:\n" + ctx.toString() + "\nQUESTION:\n" + message;

            if (openAIClient != null && openAIClient.isEnabled()) {
                String reply = openAIClient.chatCompletion(prompt);
                r.put("reply", reply);
                r.put("type", "llm_rag");
                return r;
            }
        } catch (Exception ex) {
            // if retrieval or LLM fails, fall back to prototype
        }

        // Fallback behaviors
        String lower = message.toLowerCase();
        if (lower.contains("quiz") || lower.contains("generate quiz")) {
            r.put("reply", "Here is a quick 3-question quiz:\n1) What is ...?\n2) Explain ...\n3) Solve ...");
            r.put("type", "quiz");
        } else if (lower.contains("summarize")) {
            r.put("reply", "To summarize, focus on the main concepts: A, B, and C. Revise examples and solve past papers.");
            r.put("type", "summary");
        } else {
            r.put("reply", "StudyBuzz: I understood your message. Here's a brief explanation — (prototype): " + message);
            r.put("type", "explain");
        }
        return r;
    }

    // PDF summarization uses LLM if available. 'content' should be extracted text from PDF (not the URL).
    public Map<String, Object> summarizePdf(String userId, String content) {
        Map<String, Object> r = new HashMap<>();
        if (content == null || content.isBlank()) {
            r.put("title", "Empty document");
            r.put("summary", "No content provided");
            r.put("length", 0);
            return r;
        }

        String prompt;
        // limit content length to avoid huge prompts
        if (content.length() > 6000) {
            prompt = "Summarize the following text. Provide a concise bullet-point summary and key takeaways:\n" + content.substring(0, 6000);
        } else {
            prompt = "Summarize the following text. Provide a concise bullet-point summary and key takeaways:\n" + content;
        }

        if (openAIClient != null && openAIClient.isEnabled()) {
            try {
                String summary = openAIClient.chatCompletion(prompt);
                r.put("title", "PDF Summary");
                r.put("summary", summary);
                r.put("length", summary.length());
                return r;
            } catch (Exception ex) {
                r.put("title", "PDF Summary (fallback)");
                r.put("summary", "(LLM error) " + ex.getMessage());
                r.put("length", 0);
                return r;
            }
        }

        r.put("title", "Sample PDF Summary");
        r.put("summary", "This is a placeholder summary. Extracted content length: " + Math.min(content.length(), 200) + " chars...");
        r.put("length", Math.min(content.length(), 200));
        return r;
    }
}
