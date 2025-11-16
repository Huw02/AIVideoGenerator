package org.example.aivideogenerator.service;

import org.example.aivideogenerator.model.Video;
import org.example.aivideogenerator.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VideoService {

    @Autowired
    VideoRepository videoRepository;


    public Video addVideo(Video video){
        return videoRepository.save(video);
    }

    public void updateVideo(Video video){
        videoRepository.save(video);
    }

    public List<Video>getAllVideos(){
        return videoRepository.findAll();
    }

    public Video getVideoById(int id){
        return videoRepository.findById(id).orElseThrow(() -> new RuntimeException("video not found"));
    }

    public List<Video>getVideosByProjectId(int id){
        return videoRepository.findByProjectId(id);
    }

    public void deleteVideo(int id){
        videoRepository.deleteById(id);
    }
    public Video findByVideoId(String videoId){
        return videoRepository.findByVideoId(videoId);
    }
    public Video findById(Long id){
        return videoRepository.findById(id);
    }


}
