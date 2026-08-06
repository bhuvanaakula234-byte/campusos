package com.campusos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAIClient {

    private final String apiKey;
    private final String model;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenAIClient(@Value("${openai.api.key:}") String apiKey, @Value("${openai.model:gpt-3.5-turbo}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String chatCompletion(String prompt) throws IOException, InterruptedException {
        if (!isEnabled()) throw new IllegalStateException("OpenAI API key not configured");
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", prompt));
        body.put("messages", messages);
        String reqBody = mapper.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .timeout(Duration.ofSeconds(30))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            JsonNode root = mapper.readTree(resp.body());
            JsonNode choice = root.path("choices").get(0);
            if (choice != null) {
                String text = choice.path("message").path("content").asText();
                return text;
            }
            return "";
        } else {
            throw new IOException("OpenAI API error: " + resp.statusCode() + " - " + resp.body());
        }
    }

    // Get embeddings for input text using OpenAI embeddings API
    public double[] getEmbedding(String input) throws IOException, InterruptedException {
        if (!isEnabled()) throw new IllegalStateException("OpenAI API key not configured");
        Map<String, Object> body = new HashMap<>();
        body.put("model", "text-embedding-3-small");
        body.put("input", input);
        String reqBody = mapper.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/embeddings"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .timeout(Duration.ofSeconds(30))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            JsonNode root = mapper.readTree(resp.body());
            JsonNode emb = root.path("data").get(0).path("embedding");
            if (emb != null && emb.isArray()) {
                double[] vec = new double[emb.size()];
                for (int i = 0; i < emb.size(); i++) vec[i] = emb.get(i).asDouble();
                return vec;
            }
            return new double[0];
        } else {
            throw new IOException("OpenAI embeddings API error: " + resp.statusCode() + " - " + resp.body());
        }
    }
}

