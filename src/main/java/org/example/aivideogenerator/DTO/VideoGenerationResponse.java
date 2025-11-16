package org.example.aivideogenerator.DTO;

public class VideoGenerationResponse {
    private String videoId;
    private String status;
    private String videoUrl;

    public VideoGenerationResponse() {}

    public VideoGenerationResponse(String videoId, String status, String videoUrl) {
        this.videoId = videoId;
        this.status = status;
        this.videoUrl = videoUrl;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}