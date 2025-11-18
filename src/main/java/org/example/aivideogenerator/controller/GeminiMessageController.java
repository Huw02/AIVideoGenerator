package org.example.aivideogenerator.controller;

import org.example.aivideogenerator.DTO.GeminiMessageDTO;
import org.example.aivideogenerator.model.GeminiMessage;
import org.example.aivideogenerator.service.GeminiMessageService;

import org.example.aivideogenerator.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class GeminiMessageController {

    @Autowired
    private GeminiMessageService geminiMessageService;

    @Autowired
    private ProjectService projectService;


    //følgende endpoint sender en prompt til gemini, og gemmer prompt, gemini response og projectId i db;
    @GetMapping("/gemini/askPrompt/postman")
    public String sendPromtToGemini(@RequestBody GeminiMessageDTO geminiMessageDTO){
        System.out.println(geminiMessageDTO.toString());
        String geminiResponse = geminiMessageService.explainAI(geminiMessageDTO.prompt());

        GeminiMessage geminiMessage = new GeminiMessage();
        geminiMessage.setPrompt(geminiMessageDTO.prompt());
        geminiMessage.setJsonResponse(geminiResponse);
        geminiMessage.setProject(projectService.getProjectByProjectId(geminiMessageDTO.projectId()));

        geminiMessageService.addGeminiMessage(geminiMessage);

        return geminiResponse;
    }

    //følgende metode henter en gemini request fra backend, sender til gemini og opdatere så den i DB ved at sætte response på
    @GetMapping("/gemini/askPrompt/{geminiId}")
    public ResponseEntity<GeminiMessage> sendPromtToGeminiFromBackend(@PathVariable int geminiId){

        GeminiMessage geminiMessage = geminiMessageService.getGeminiMessageByGeminiId(geminiId);

        String geminiResponse = geminiMessageService.explainAI(geminiMessage.getPrompt());

        geminiMessage.setJsonResponse(geminiResponse);

        geminiMessageService.addGeminiMessage(geminiMessage);


        return new ResponseEntity<>(geminiMessageService.addGeminiMessage(geminiMessage), HttpStatus.OK);
    }




    @PostMapping("/gemini/makePrompt")
    public ResponseEntity<GeminiMessage>createGeminiPrompt(@RequestBody GeminiMessageDTO geminiMessageDTO){

        GeminiMessage geminiMessage = new GeminiMessage();
        geminiMessage.setProject(projectService.getProjectByProjectId(geminiMessageDTO.projectId()));
        geminiMessage.setPrompt(geminiMessageDTO.prompt());

        return new ResponseEntity<>(geminiMessageService.addGeminiMessage(geminiMessage), HttpStatus.CREATED);
    }


    //metode bruges ikke endnu
    /*
    @GetMapping("/gemini/{geminiId}")
    public ResponseEntity<GeminiMessage>getGeminiById(@PathVariable int geminiId){
        return new ResponseEntity<>(geminiMessageService.getGeminiMessageByGeminiId(geminiId), HttpStatus.OK);
    }
    */



    @GetMapping("/gemini/{projectId}")
    public ResponseEntity<List<GeminiMessage>>getGeminiPromptsByProjectId(@PathVariable int projectId){
        return new ResponseEntity<>(geminiMessageService.findByProjectId(projectId), HttpStatus.OK);
    }

    @DeleteMapping("/gemini/{geminiId}")
    public ResponseEntity<String>deleteGeminiMessage(@PathVariable int geminiId){
        GeminiMessage geminiMessage = geminiMessageService.getGeminiMessageByGeminiId(geminiId);
        if(geminiMessage != null){
            geminiMessageService.deleteGeminiMessage(geminiId);
            return ResponseEntity.ok("Gemini message was deleted");
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Could not find gemini message by that id");
        }


    }







}
