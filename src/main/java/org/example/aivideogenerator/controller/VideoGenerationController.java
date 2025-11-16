package org.example.aivideogenerator.controller;



import org.example.aivideogenerator.DTO.VideoGenerationRequest;
import org.example.aivideogenerator.DTO.VideoGenerationResponse;
import org.example.aivideogenerator.service.VideoGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin(origins = "*")
public class VideoGenerationController {

    private static final Logger logger = LoggerFactory.getLogger(VideoGenerationController.class);

    @Autowired
    private VideoGenerationService videoGenerationService;

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

    // Test endpoint to verify the API is working
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        logger.info("Test endpoint called");
        return ResponseEntity.ok("Video API is running!");
    }
}