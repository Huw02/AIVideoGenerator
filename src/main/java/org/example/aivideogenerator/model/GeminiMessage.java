package org.example.aivideogenerator.model;

import jakarta.persistence.*;

@Entity
public class GeminiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    //bruger Lob for at kunne gemme større mængder data i DB, promt er en string
    @Lob
    private String prompt;

    //jsonResponse er en string der indeholder det JSON response der kommer fra gemini
    @Lob
    private String jsonResponse;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "video_id")
    private Video video;

    public GeminiMessage(int id, String jsonResponse, String prompt) {
        this.id = id;
        this.jsonResponse = jsonResponse;
        this.prompt = prompt;
    }

    public GeminiMessage(String jsonResponse, String prompt) {
        this.jsonResponse = jsonResponse;
        this.prompt = prompt;
    }

    public GeminiMessage(){

    }

    public int getId() {
        return id;
    }

    public void setId(int geminiId) {
        this.id = geminiId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String promt) {
        this.prompt = promt;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }

    public void setJsonResponse(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }
}
