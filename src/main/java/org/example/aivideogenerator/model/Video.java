package org.example.aivideogenerator.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String videoTitle;

    private String videoLink;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "gemini_id")
    private GeminiMessage geminiMessage;

    @ManyToOne
    @JoinColumn(name ="project_id")
    @JsonBackReference
    private Project project;



    public Video(int id, String videoTitle, String videoLink, GeminiMessage geminiMessage) {
        this.id = id;
        this.videoTitle = videoTitle;
        this.videoLink = videoLink;
        this.geminiMessage = geminiMessage;
    }
    public Video(){

    }

    public int getId() {
        return id;
    }

    public void setId(int videoId) {
        this.id = videoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    public GeminiMessage getGeminiMessage() {
        return geminiMessage;
    }

    public void setGeminiMessage(GeminiMessage geminiMessage) {
        this.geminiMessage = geminiMessage;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
