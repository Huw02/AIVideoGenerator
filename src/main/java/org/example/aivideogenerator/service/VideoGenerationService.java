package org.example.aivideogenerator.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aivideogenerator.DTO.VideoGenerationResponse;
import org.example.aivideogenerator.model.Video;
import org.example.aivideogenerator.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
public class VideoGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(VideoGenerationService.class);
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    @Value("${google.veo3.api.key}")
    private String apiKey;

    @Value("${video.storage.path:videos/}")
    private String storagePath;

    @Autowired
    private VideoRepository videoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoGenerationResponse generateVideo(String prompt) {
        logger.info("Starting video generation with prompt: {}", prompt);

        try {
            String apiUrl = BASE_URL + "/models/veo-3.1-generate-preview:predictLongRunning";

            logger.info("API URL: {}", apiUrl);

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-goog-api-key", apiKey);
            conn.setDoOutput(true);

            // Correct payload structure for Veo 3.1
            String jsonPayload = String.format(
                    "{\"instances\": [{\"prompt\": \"%s\"}]}",
                    prompt.replace("\"", "\\\"").replace("\n", "\\n")
            );

            logger.info("Request payload: {}", jsonPayload);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            logger.info("Response code: {}", responseCode);

            // Read response
            InputStream inputStream;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }

            String responseBody = "";
            if (inputStream != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                responseBody = response.toString();
            }

            logger.info("API Response: {}", responseBody);

            if (responseCode >= 200 && responseCode < 300) {
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                // Get operation name from response
                String operationName = jsonResponse.has("name")
                        ? jsonResponse.get("name").asText()
                        : null;

                if (operationName == null) {
                    throw new RuntimeException("No operation name returned from API");
                }

                logger.info("Operation name: {}", operationName);

                // Save to database with operation name as video ID
                Video video = new Video();
                video.setPrompt(prompt);
                video.setVideoId(operationName);
                video.setStatus("PROCESSING");
                video.setCreatedAt(LocalDateTime.now());

                try {
                    videoRepository.save(video);
                    logger.info("Video saved to database successfully");
                } catch (Exception e) {
                    logger.error("Failed to save video to database", e);
                }

                return new VideoGenerationResponse(operationName, "PROCESSING", null);
            } else {
                String errorMessage = String.format(
                        "API Error - Status: %d, Response: %s",
                        responseCode,
                        responseBody
                );
                logger.error(errorMessage);

                // Parse error message if available
                try {
                    JsonNode errorJson = objectMapper.readTree(responseBody);
                    if (errorJson.has("error")) {
                        JsonNode error = errorJson.get("error");
                        if (error.has("message")) {
                            errorMessage = "API Error: " + error.get("message").asText();
                        }
                    }
                } catch (Exception e) {
                    // Keep original error message
                }

                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            logger.error("Exception in generateVideo", e);
            throw new RuntimeException("Error generating video: " + e.getMessage(), e);
        }
    }

    public VideoGenerationResponse checkVideoStatus(String operationName) {
        logger.info("Checking status for operation: {}", operationName);

        try {
            // Check database first
            Video video = videoRepository.findByVideoId(operationName);
            if (video == null) {
                logger.warn("Video not found in database: {}", operationName);
                return new VideoGenerationResponse(operationName, "NOT_FOUND", null);
            }

            // Check operation status
            String apiUrl = BASE_URL + "/" + operationName;

            logger.info("Status check URL: {}", apiUrl);

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("x-goog-api-key", apiKey);

            int responseCode = conn.getResponseCode();
            logger.info("Status check response code: {}", responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                InputStream inputStream = conn.getInputStream();
                if (inputStream != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    String responseBody = response.toString();
                    logger.info("Status response: {}", responseBody);

                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    boolean done = jsonResponse.has("done") && jsonResponse.get("done").asBoolean();
                    String status = done ? "COMPLETED" : "PROCESSING";

                    // Update database
                    video.setStatus(status);
                    videoRepository.save(video);

                    String videoUri = null;
                    if (done && jsonResponse.has("response")) {
                        JsonNode responseNode = jsonResponse.get("response");
                        if (responseNode.has("generateVideoResponse")) {
                            JsonNode generateVideoResponse = responseNode.get("generateVideoResponse");
                            if (generateVideoResponse.has("generatedSamples")
                                    && generateVideoResponse.get("generatedSamples").isArray()
                                    && generateVideoResponse.get("generatedSamples").size() > 0) {
                                JsonNode firstSample = generateVideoResponse.get("generatedSamples").get(0);
                                if (firstSample.has("video") && firstSample.get("video").has("uri")) {
                                    videoUri = firstSample.get("video").get("uri").asText();
                                }
                            }
                        }
                    }

                    return new VideoGenerationResponse(operationName, status, videoUri);
                }
            }

            // If API call fails, return database status
            logger.warn("API status check failed, returning database status");
            return new VideoGenerationResponse(operationName, video.getStatus(), null);

        } catch (Exception e) {
            logger.error("Exception in checkVideoStatus", e);
            // Return database status on error
            Video video = videoRepository.findByVideoId(operationName);
            if (video != null) {
                return new VideoGenerationResponse(operationName, video.getStatus(), null);
            }
            return new VideoGenerationResponse(operationName, "ERROR", null);
        }
    }

    public VideoGenerationResponse downloadAndSaveVideo(String operationName) {
        logger.info("Downloading video for operation: {}", operationName);

        try {
            // First check status to get video URL
            VideoGenerationResponse statusResponse = checkVideoStatus(operationName);

            if (!"COMPLETED".equals(statusResponse.getStatus())) {
                return new VideoGenerationResponse(operationName, "Video not ready yet", null);
            }

            String videoUri = statusResponse.getVideoUrl();
            if (videoUri == null || videoUri.isEmpty()) {
                throw new RuntimeException("Video URI not available");
            }

            // Create storage directory if it doesn't exist
            Path storageDirPath = Paths.get(storagePath);
            if (!Files.exists(storageDirPath)) {
                Files.createDirectories(storageDirPath);
                logger.info("Created storage directory: {}", storageDirPath);
            }

            // Generate filename from operation name
            String fileName = operationName.replaceAll("[^a-zA-Z0-9]", "_") + ".mp4";
            Path filePath = storageDirPath.resolve(fileName);

            logger.info("Downloading video from: {}", videoUri);
            logger.info("Saving to: {}", filePath);

            // Download video with API key header and follow redirects
            HttpURLConnection conn = null;
            URL url = new URL(videoUri);

            // Handle redirects manually to maintain headers
            boolean redirect = false;
            int redirectCount = 0;
            final int MAX_REDIRECTS = 5;

            do {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("x-goog-api-key", apiKey);
                conn.setInstanceFollowRedirects(false);

                int status = conn.getResponseCode();
                redirect = (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER);

                if (redirect) {
                    String newUrl = conn.getHeaderField("Location");
                    logger.info("Redirecting to: {}", newUrl);
                    url = new URL(newUrl);
                    redirectCount++;

                    if (redirectCount > MAX_REDIRECTS) {
                        throw new RuntimeException("Too many redirects");
                    }
                }
            } while (redirect);

            // Download the file
            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(filePath.toFile())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                logger.info("Video downloaded successfully. Total bytes: {}", totalBytes);
            }

            // Update database with file path
            Video video = videoRepository.findByVideoId(operationName);
            if (video != null) {
                video.setFilePath(filePath.toString());
                video.setStatus("DOWNLOADED");
                videoRepository.save(video);
            }

            return new VideoGenerationResponse(operationName, "DOWNLOADED", filePath.toString());
        } catch (Exception e) {
            logger.error("Exception in downloadAndSaveVideo", e);
            throw new RuntimeException("Error downloading video: " + e.getMessage(), e);
        }
    }

    public Video findByVideoId(String videoId){
        return videoRepository.findByVideoId(videoId);
    }
}