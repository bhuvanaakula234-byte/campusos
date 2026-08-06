package com.campusos.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "embeddings")
public class Embedding {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(columnDefinition = "text")
    private String content;

    // store vector as double precision[] in Postgres
    @Column(name = "vector", columnDefinition = "double precision[]")
    private Double[] vector;

    @Column(columnDefinition = "text")
    private String metadata;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Double[] getVector() { return vector; }
    public void setVector(Double[] vector) { this.vector = vector; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
