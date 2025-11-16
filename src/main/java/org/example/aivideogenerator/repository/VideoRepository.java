package org.example.aivideogenerator.repository;

import org.example.aivideogenerator.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {
    List<Video>findByProjectId(int projectId);
    Video findByVideoId(String videoId);
    Video findById(Long id);
}
