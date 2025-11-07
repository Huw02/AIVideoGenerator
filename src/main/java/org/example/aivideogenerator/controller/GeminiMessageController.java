package org.example.aivideogenerator.controller;

import org.example.aivideogenerator.DTO.GeminiMessageDTO;
import org.example.aivideogenerator.model.GeminiMessage;
import org.example.aivideogenerator.service.GeminiMessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class GeminiMessageController {

    @Autowired
    private GeminiMessageService geminiMessageService;

    @GetMapping("/gemini/askPrompt")
    public String sendPromtToGemini(@RequestBody GeminiMessageDTO geminiMessageDTO){
        String geminiResponse = geminiMessageService.explainAI(geminiMessageDTO.prompt());

        GeminiMessage geminiMessage = new GeminiMessage();
        geminiMessage.setPrompt(geminiMessageDTO.prompt());
        geminiMessage.setJsonResponse(geminiResponse);

        geminiMessageService.addGeminiMessage(geminiMessage);


        return geminiResponse;
    }


    //bruges ikke
    @GetMapping("/gemini/sendPrompt")
    public ResponseEntity<String> generateVeo3Json(@RequestBody GeminiMessageDTO geminiMessageDTO) {
        System.out.println(geminiMessageDTO.prompt());
        if(geminiMessageDTO.prompt() != null) {
            String json = geminiMessageService.generateVeo3Json(geminiMessageDTO);

            GeminiMessage geminiMessage = new GeminiMessage();
            geminiMessage.setPrompt(geminiMessageDTO.prompt());
            geminiMessage.setJsonResponse(json);

            geminiMessageService.addGeminiMessage(geminiMessage);

            return ResponseEntity.ok(json);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The dto promt was empty");
        }
    }

    @GetMapping("/gemini/{geminiId}")
    public ResponseEntity<GeminiMessage>getGeminiById(@PathVariable int geminiId){
        return new ResponseEntity<>(geminiMessageService.getGeminiMessageByGeminiId(geminiId), HttpStatus.OK);
    }

    @PostMapping("/gemini")
    public ResponseEntity<GeminiMessage>addGemini(@RequestBody GeminiMessageDTO geminiMessageDTO){
        GeminiMessage geminiMessage = new GeminiMessage();

        geminiMessage.setPrompt(geminiMessageDTO.prompt());
        //geminiMessage.setJsonResponse(geminiMessageDTO.jsonResponse());

        return new ResponseEntity<>(geminiMessageService.addGeminiMessage(geminiMessage), HttpStatus.CREATED);
    }







}
