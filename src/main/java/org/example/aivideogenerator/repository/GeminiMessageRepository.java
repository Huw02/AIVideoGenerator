package org.example.aivideogenerator.repository;

import org.example.aivideogenerator.model.GeminiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeminiMessageRepository extends JpaRepository<GeminiMessage, Integer> {
    GeminiMessage findByVideo_id(int id);
    List<GeminiMessage>findByProject_id(int id);
}
