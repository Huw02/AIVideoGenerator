package org.example.aivideogenerator.controller;


import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.example.aivideogenerator.DTO.UpdateGeminiMessageRequest;
import org.example.aivideogenerator.DTO.VideoGenerationRequest;
import org.example.aivideogenerator.DTO.VideoGenerationResponse;
import org.example.aivideogenerator.model.GeminiMessage;
import org.example.aivideogenerator.model.Video;
import org.example.aivideogenerator.service.GeminiMessageService;
import org.example.aivideogenerator.service.VideoGenerationService;
import org.example.aivideogenerator.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin(origins = "*")
public class VideoGenerationController {

    private static final Logger logger = LoggerFactory.getLogger(VideoGenerationController.class);

    @Autowired
    private VideoGenerationService videoGenerationService;

    @Autowired
    private GeminiMessageService geminiMessageService;

    @Autowired
    private VideoService videoService;


    @PostMapping("/generate")
    public ResponseEntity<VideoGenerationResponse> generateVideo(@RequestBody VideoGenerationRequest request) {
        logger.info("Received video generation request with prompt: {}", request.getPrompt());

        try {
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                logger.warn("Empty prompt received");
                return ResponseEntity.badRequest()
                        .body(new VideoGenerationResponse(null, "Prompt cannot be empty", null));
            }

            VideoGenerationResponse response = videoGenerationService.generateVideo(request.getPrompt());
            logger.info("Video generation initiated successfully: {}", response.getVideoId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating video", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VideoGenerationResponse(null, "Error: " + errorMessage, null));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<VideoGenerationResponse> checkStatus(@RequestParam String operationName) {
        logger.info("Checking status for operation: {}", operationName);

        try {
            VideoGenerationResponse response = videoGenerationService.checkVideoStatus(operationName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking video status", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VideoGenerationResponse(operationName, "Error: " + errorMessage, null));
        }
    }

    @PostMapping("/download")
    public ResponseEntity<VideoGenerationResponse> downloadVideo(@RequestParam String operationName) {
        logger.info("Download request for operation: {}", operationName);

        try {
            VideoGenerationResponse response = videoGenerationService.downloadAndSaveVideo(operationName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error downloading video", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VideoGenerationResponse(operationName, "Error: " + errorMessage, null));
        }
    }

    @PostMapping("/updatedGeminiMessage")
    public ResponseEntity<String> updateGeminiMessage(@RequestBody UpdateGeminiMessageRequest request) {
        System.out.println("post er kaldt");
            if(request == null){
                return null;
            }

            GeminiMessage geminiMessage = geminiMessageService.getGeminiMessageByGeminiId(request.id());
            geminiMessage.setVideo(videoService.findById(request.videoId()));

            geminiMessageService.addGeminiMessage(geminiMessage);
        System.out.println("den har opdateret geminimsg: " + geminiMessage);
            return ResponseEntity.ok("The gemini message has been updated with a videoId");
    }

    @GetMapping("/updatedGeminiMessage/{videoId}")
    public ResponseEntity<Long>updateGeminiVideoId(@PathVariable String videoId){
        System.out.println("get er kaldt");
        if(videoId.isEmpty()){
            return null;
        }
        Video video = videoGenerationService.findByVideoId(videoId);
        System.out.println("den har fundet videoen: " + video);
        return ResponseEntity.ok(video.getId());
    }



    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> streamVideo(@PathVariable Long videoId) {
        logger.info("Streaming video with ID: {}", videoId);

        try {
            Video video = videoService.findById(videoId);
            if (video == null || video.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }

            // Normalize the path (convert backslashes to forward slashes)
            String normalizedPath = video.getFilePath().replace("\\", "/");
            Path videoPath = Paths.get(normalizedPath);

            if (!Files.exists(videoPath)) {
                logger.error("Video file not found at path: {}", videoPath);
                return ResponseEntity.notFound().build();
            }

            Resource videoResource = new UrlResource(videoPath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + videoPath.getFileName().toString() + "\"")
                    .body(videoResource);

        } catch (Exception e) {
            logger.error("Error streaming video", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




}