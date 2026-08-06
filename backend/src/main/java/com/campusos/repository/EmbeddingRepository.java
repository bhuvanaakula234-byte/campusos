package com.campusos.repository;

import com.campusos.model.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {
}
