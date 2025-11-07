package org.example.aivideogenerator.service;

import org.example.aivideogenerator.model.Project;
import org.example.aivideogenerator.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    ProjectRepository projectRepository;

    public Project addProject(Project project){
        return projectRepository.save(project);
    }

    public void updateProject(Project project){
        projectRepository.save(project);
    }

    public List<Project>getAllProjects(){
        return projectRepository.findAll();
    }

    public Project getProjectByProjectId(int id){
        return projectRepository.findById(id).orElseThrow(()-> new RuntimeException("could not find project"));
    }

    public List<Project>getProjectsByUserId(int id){
        return projectRepository.findByUserId(id);
    }


    public void deleteProject(int id){
        projectRepository.deleteById(id);
    }


}
