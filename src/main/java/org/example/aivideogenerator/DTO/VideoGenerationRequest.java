package org.example.aivideogenerator.DTO;

public class VideoGenerationRequest {
    private String prompt;

    public VideoGenerationRequest() {}

    public VideoGenerationRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}