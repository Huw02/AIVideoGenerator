package org.example.aivideogenerator.repository;

import org.example.aivideogenerator.model.GeminiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeminiMessageRepository extends JpaRepository<GeminiMessage, Integer> {
    GeminiMessage findByVideo_id(int id);
}
