package org.example.aivideogenerator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.example.aivideogenerator.DTO.GeminiMessageDTO;
import org.example.aivideogenerator.model.GeminiMessage;
import org.example.aivideogenerator.repository.GeminiMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;
import org.json.JSONObject;

import java.util.List;
import java.util.Scanner;

@Service
public class GeminiMessageService {

    @Autowired
    GeminiMessageRepository geminiMessageRepository;


    private static final String SYSTEM_PROMPT = """
        You are an assistant that helps creators turn natural language video ideas into structured JSON prompts for Google Veo 3. You always follow a consistent format.

        Your job:

        Take user ideas written in natural language

        Return a complete, properly structured JSON prompt

        Maintain cinematic, specific, and visually rich language

        Ask clarifying questions if the idea is unclear

        JSON prompts must follow this structure:
        {
          "description": "Cinematic summary of the scene-what happens visually",
          "style": "Visual mood or aesthetic (e.g. cinematic, magical realism)",
          "camera": "Camera movement or framing (e.g. dolly-in, fixed wide shot)",
          "lens": "Lens or framing type (optional)",
          "lighting": "How the scene is lit (e.g. neon, sunset, natural glow)",
          "environment": "Scene location or space (optional)",
          "audio": "Music or sound design if specified (optional)",
          "elements": ["List of objects, subjects, or visual items that must appear"],
          "motion": "How objects move or transform in the scene",
          "ending": "What the final visual moment or shot looks like",
          "text": "Usually 'none' unless on-screen text is mentioned",
          "keywords": ["descriptive tags that reinforce theme, tone, or subject"]
        }

        Always write visually, cinematic, and clear. You must only return a String in the JSON format, so it can be send to veo3, dont return anything else. 
        The JSON you generate will be send directly to VEO3 to generate a video, so do not use any variables or ask any questions. Only send a JSON that can be directly used.
        Everything after this line is the prompt for Veo3 JSON.
        """;



    //BRUGES NU
    public String explainAI(String prompt) {
        Client client = new Client();
        String finishPromt = SYSTEM_PROMPT + prompt;

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash",
                        finishPromt,
                        null);

        System.out.println(response.text());
        return response.text();
    }




    public GeminiMessage addGeminiMessage(GeminiMessage geminiMessage){
        return geminiMessageRepository.save(geminiMessage);
    }

    public GeminiMessage getGeminiMessageByVideoId(int videoId){
        return geminiMessageRepository.findByVideo_id(videoId);
    }

    public GeminiMessage getGeminiMessageByGeminiId(int geminiId){
        return geminiMessageRepository.findById(geminiId).orElseThrow(() -> new RuntimeException("could not find gemini message"));
    }

    public List<GeminiMessage>findByProjectId(int id){
        return geminiMessageRepository.findByProject_id(id);
    }
    public void deleteGeminiMessage(int geminiId){
        geminiMessageRepository.deleteById(geminiId);
    }





}
