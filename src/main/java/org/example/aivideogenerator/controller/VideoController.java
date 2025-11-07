package org.example.aivideogenerator.controller;

import org.example.aivideogenerator.DTO.VideoDTO;
import org.example.aivideogenerator.model.Video;
import org.example.aivideogenerator.service.GeminiMessageService;
import org.example.aivideogenerator.service.ProjectService;
import org.example.aivideogenerator.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class VideoController {


    @Autowired
    VideoService videoService;

    @Autowired
    ProjectService projectService;

    @Autowired
    GeminiMessageService geminiMessageService;

    @GetMapping("/videos/{projectId}")
    public ResponseEntity<List<Video>>getVideosByProjectId(@PathVariable int projectId){
        return new ResponseEntity<>(videoService.getVideosByProjectId(projectId), HttpStatus.OK);
    }

    @PostMapping("/videos")
    public ResponseEntity<Video>addVideo(@RequestBody VideoDTO videoDTO){
        Video video = new Video();

        video.setProject(projectService.getProjectByProjectId(videoDTO.projectId()));
        video.setGeminiMessage(geminiMessageService.getGeminiMessageByGeminiId(videoDTO.geminiId()));
        video.setVideoTitle(videoDTO.videoTitle());
        video.setVideoLink(videoDTO.videoLink());
        return new ResponseEntity<>(videoService.addVideo(video), HttpStatus.CREATED);
    }

    @DeleteMapping("/videos/{videoId}")
    public ResponseEntity<String>deleteVideo(@PathVariable int videoId){
        Video checkVideoId = videoService.getVideoById(videoId);
        if(checkVideoId != null){
            videoService.deleteVideo(videoId);
            return ResponseEntity.ok("deleted video");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("video not found");
        }
    }

    @PutMapping("/videos/{videoId}")
    public ResponseEntity<String>updateVideo(@PathVariable int videoId, @RequestBody VideoDTO videoDTO){
        Video checkVideoId = videoService.getVideoById(videoId);
        if(checkVideoId != null){
            Video video = new Video();

            video.setId(videoId);
            video.setVideoTitle(videoDTO.videoTitle());
            video.setVideoLink(videoDTO.videoLink());

            videoService.updateVideo(video);
            return ResponseEntity.ok("video has been updated");
        }else {
            return ResponseEntity.notFound().build();
        }
    }

}
