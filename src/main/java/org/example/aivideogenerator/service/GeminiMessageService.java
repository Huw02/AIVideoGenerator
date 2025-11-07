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

import java.util.Scanner;

@Service
public class GeminiMessageService {

    @Autowired
    GeminiMessageRepository geminiMessageRepository;


    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta3/models/gemini-2.5-pro:generateMessage}")
    private String geminiApiUrl;

    private final ObjectMapper mapper = new ObjectMapper();

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

    //bruges ikke lige nu
    public String generateVeo3Json(GeminiMessageDTO dto) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Combine system prompt + user prompt
            String fullPrompt = SYSTEM_PROMPT + "\n\n" + dto.prompt();

            // Build JSON body
            ObjectNode textNode = mapper.createObjectNode();
            textNode.put("type", "text");
            textNode.put("text", fullPrompt);

            ArrayNode contentArray = mapper.createArrayNode();
            contentArray.add(textNode);

            ObjectNode messageNode = mapper.createObjectNode();
            messageNode.put("role", "user");
            messageNode.set("content", contentArray);

            ArrayNode messagesArray = mapper.createArrayNode();
            messagesArray.add(messageNode);

            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.set("messages", messagesArray);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(geminiApiKey);

            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);

            // Call Gemini API
            ResponseEntity<String> response = restTemplate.exchange(
                    geminiApiUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return extractJsonFromText(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }


    //bruges ikke lige nu
    // Extract JSON from Gemini response text
    private String extractJsonFromText(String responseText) {
        try {
            JsonNode root = mapper.readTree(responseText);
            JsonNode textNode = root.path("candidates").get(0).path("content").path("text");
            String jsonString = textNode.asText();

            // Optional: validate JSON
            JsonNode parsed = mapper.readTree(jsonString);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini JSON output: " + e.getMessage(), e);
        }
    }


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





}
