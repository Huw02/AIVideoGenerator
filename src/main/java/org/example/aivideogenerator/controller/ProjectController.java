package org.example.aivideogenerator.controller;


import org.example.aivideogenerator.DTO.ProjectDTO;
import org.example.aivideogenerator.model.Project;
import org.example.aivideogenerator.model.User;
import org.example.aivideogenerator.service.ProjectService;
import org.example.aivideogenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;


    @GetMapping("/admin/projects")
    public ResponseEntity<List<Project>>getAllProjects(){
        return new ResponseEntity<>(projectService.getAllProjects(), HttpStatus.OK);
    }

    //endpoint så en admin kan tilgå en specifik users projects ved userId
    @GetMapping("/admin/projects/{userId}")
    public ResponseEntity<List<Project>>getProjectsByUserId(@PathVariable int userId){
        return new ResponseEntity<>(projectService.getProjectsByUserId(userId), HttpStatus.OK);
    }

    //endpoints så en user kan se dets egne projects
    @GetMapping("/projects/me")
    public ResponseEntity<List<Project>> getMyProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User loggedInUser = userService.getUserByUsername(username);

        List<Project> projects = projectService.getProjectsByUserId(loggedInUser.getId());
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @PostMapping("/projects")
    public ResponseEntity<Project>addProject(@RequestBody ProjectDTO projectDTO){
        Project project = new Project();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        int userId = userService.getUserByUsername(username).getId();


        project.setUser(userService.getUser(userId));
        project.setProjectName(projectDTO.projectName());
        project.setProjectDescription(projectDTO.projectDescription());
        return new ResponseEntity<>(projectService.addProject(project), HttpStatus.CREATED);
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<String>deleteProject(@PathVariable int projectId){
        Project checkProjectId = projectService.getProjectByProjectId(projectId);
        if(checkProjectId != null){
            projectService.deleteProject(projectId);
            return ResponseEntity.ok("project deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("could not find project");
        }
    }

    @PutMapping("/projects/{projectId}")
    public ResponseEntity<String>updateProject(@PathVariable int projectId, @RequestBody ProjectDTO projectDTO){
        Project checkProjectId = projectService.getProjectByProjectId(projectId);
        if(checkProjectId != null){
            Project project = new Project();

            project.setId(projectId);
            project.setProjectName(projectDTO.projectName());
            project.setProjectDescription(projectDTO.projectDescription());

            projectService.updateProject(project);
            return ResponseEntity.ok("project has been updated");
        } else {
            return ResponseEntity.notFound().build();
        }

    }








}
