package com.campusos.service;

import com.campusos.model.Embedding;
import com.campusos.repository.EmbeddingRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmbeddingService {

    private final EmbeddingRepository embeddingRepository;
    private final OpenAIClient openAIClient;

    public EmbeddingService(EmbeddingRepository embeddingRepository, OpenAIClient openAIClient) {
        this.embeddingRepository = embeddingRepository;
        this.openAIClient = openAIClient;
    }

    public Embedding create(String content, String metadata) {
        try {
            double[] emb = openAIClient.getEmbedding(content);
            Double[] vec = new Double[emb.length];
            for (int i = 0; i < emb.length; i++) vec[i] = emb[i];
            Embedding e = new Embedding();
            e.setContent(content);
            e.setVector(vec);
            e.setMetadata(metadata);
            return embeddingRepository.save(e);
        } catch (IOException | InterruptedException ex) {
            // fallback: save without vector
            Embedding e = new Embedding();
            e.setContent(content);
            e.setVector(new Double[0]);
            e.setMetadata(metadata);
            return embeddingRepository.save(e);
        }
    }

    // naive retrieval: fetch all and compute cosine similarity
    public List<Map<String, Object>> retrieve(String query, int k) {
        double[] qVec;
        try {
            qVec = openAIClient.getEmbedding(query);
        } catch (Exception ex) {
            qVec = null;
        }
        List<Embedding> all = embeddingRepository.findAll();
        List<Map<String, Object>> scored = new ArrayList<>();
        for (Embedding e : all) {
            double score = 0.0;
            if (qVec != null && e.getVector() != null && e.getVector().length==qVec.length) {
                double[] ev = Arrays.stream(e.getVector()).mapToDouble(Double::doubleValue).toArray();
                score = cosineSimilarity(qVec, ev);
            }
            Map<String,Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("content", e.getContent());
            m.put("metadata", e.getMetadata());
            m.put("score", score);
            scored.add(m);
        }
        return scored.stream().sorted((a,b)-> Double.compare((Double)b.get("score"),(Double)a.get("score"))).limit(k).collect(Collectors.toList());
    }

    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i]*b[i];
            na += a[i]*a[i];
            nb += b[i]*b[i];
        }
        if (na==0 || nb==0) return 0.0;
        return dot / (Math.sqrt(na)*Math.sqrt(nb));
    }
}
