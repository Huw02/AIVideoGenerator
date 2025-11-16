//import jwtDecode from "jwt-decode";
import {postObjectAsJson, fetchAnyUrl} from "./modulejson.js";

const API_BASE = 'http://localhost:8080/api/v1';

let form, projectsList;

document.addEventListener('DOMContentLoaded', () => {
    form = document.getElementById("createProjectForm");
    projectsList = document.getElementById("projectsList");


    form.addEventListener("submit", createProject)

    renderProjects();



});



// Render existing projects
async function renderProjects() {
    const getProjects = API_BASE + "/projects/me"; // loader alle ens egne projects
    const token = localStorage.getItem("jwt");
    console.log(token)
    console.log("JWT token from localStorage:", localStorage.getItem("jwt"));


    if (!token) {
        console.error("No JWT token found!");
        return;
    }

    const projects = await fetchAnyUrl(getProjects, true); // true betyder at vi bruger JWT token

    if (!Array.isArray(projects)) {
        console.error("Projects is not an array:", projects);
        return;
    }

    const projectsList = document.getElementById("projectsList");
    if (!projectsList) {
        console.error("projectsList element not found in DOM!");
        return;
    }

    projectsList.innerHTML = "";

    projects.forEach((proj) => {
        // Create a container for the project
        const projectItem = document.createElement("div");
        projectItem.classList.add("project-item");

        // Set inner HTML
        projectItem.innerHTML = `
            <h3>${proj.projectName}</h3>
            <p>${proj.projectDescription}</p>
            <button class="delete-btn">Delete</button>
        `;

        // Click listener to open more info about this project
        projectItem.addEventListener("click", (e) => {
            // Prevent the click from triggering the delete button
            if (e.target.classList.contains("delete-btn")) return;

            openProject(proj);
        });

        // Attach delete button listener
        const deleteBtn = projectItem.querySelector(".delete-btn");
        deleteBtn.addEventListener("click", () => deleteProject(proj));

        projectsList.appendChild(projectItem);
    });
}


async function openProject(proj){
    document.getElementById("create-project").style.display = "none";
    document.getElementById("projects-section").style.display = "none";

    const detailsSection = document.getElementById("projectDetails");
    detailsSection.className = "project-view"; // Add this
    detailsSection.style.display = "block";
    detailsSection.innerHTML = "";

    if(!detailsSection){
        return;
    }


    const backButton = document.createElement("button");
    backButton.textContent = "← Back"
    backButton.className = "back-btn";
    backButton.addEventListener("click", () => {
        // Show dashboard again
        document.getElementById("create-project").style.display = "block";
        document.getElementById("projects-section").style.display = "block";
        detailsSection.style.display = "none";
    });


    //here i load the info about the project

    const title = document.createElement("h2");
    title.textContent = proj.projectName;

    const description = document.createElement("p");
    description.textContent = proj.projectDescription;

    //here i generate the prompt

    const promptLabel = document.createElement("label");
    promptLabel.textContent = "Generate Video Prompt";

    const promptInput = document.createElement("textarea");
    promptInput.placeholder = "Enter idea for prompt..";

    const submitBtn = document.createElement("button");
    submitBtn.textContent = "Submit prompt";



    //here i load the response from gemini
    const responseBox = document.createElement("div");
    responseBox.className = "geminiResponse";
    responseBox.textContent ="Response will appear here...";


    submitBtn.addEventListener("click", async () =>{
        const userPrompt = promptInput.value.trim();
        if(!userPrompt){
            alert("Please enter a prompt!")
            return;
        }
        responseBox.textContent = "Generating response..."

        const response = await sendPromptToGemini(userPrompt, proj.id);
        console.log(response)
        if(!response){
            console.log("No response: " + response);
            return;
        }
        responseBox.textContent = response.toString();
    })
    detailsSection.appendChild(backButton)
    detailsSection.appendChild(title);
    detailsSection.appendChild(description);

    const promptSection = document.createElement("div");
    promptSection.className = "prompt-section";
    promptSection.appendChild(promptLabel);
    promptSection.appendChild(promptInput);
    promptSection.appendChild(submitBtn);

    detailsSection.appendChild(promptSection);

    // Response box
    responseBox.className = "response-box";
    detailsSection.appendChild(responseBox);


    //here i load the earlier prompts and their responses for a specific project
    const listOfPromptsForProject = await getPromptsByProjectId(proj.id);


    if (!Array.isArray(listOfPromptsForProject)) {
        console.error("List of Prompts is not an array:", listOfPromptsForProject);
        return;
    }
    if (!listOfPromptsForProject || listOfPromptsForProject.length === 0) {
        console.log("No prompts found for this project");
        return;
    }

    const oldPromptsContainer = document.createElement("div");
    oldPromptsContainer.className = "prompt-history";

    listOfPromptsForProject.forEach(singlePrompt => {
        const oldPromptBox = document.createElement("div");
        oldPromptBox.className = "prompt-item";

        const oldPromptSend = document.createElement("h3")
        oldPromptSend.textContent = singlePrompt.prompt;

        const oldPromptReceived = document.createElement("textarea");
        oldPromptReceived.textContent = singlePrompt.jsonResponse;

        oldPromptBox.appendChild(oldPromptSend);
        oldPromptBox.appendChild(oldPromptReceived);
        oldPromptsContainer.appendChild(oldPromptBox);
    })

    detailsSection.appendChild(oldPromptsContainer);

}

function loadDashboardView() {
    location.reload(); // simplest approach, reloads dashboard page
}


async function sendPromptToGemini(prompt, projectId){
    const savePromptInDB = API_BASE + "/gemini/makePrompt";
    const geminiMessageDto = { prompt, projectId };

    try {
        console.log("Sender prompt til DB");
        // postObjectAsJson should return the response body as JSON
        const response = await postObjectAsJson(savePromptInDB, geminiMessageDto, "POST", true);

        // Ensure we have JSON from the response
        const savedPrompt = await response.json?.() ?? response;

        if(savedPrompt && savedPrompt.id){
            console.log("Prompt fra DB:", savedPrompt);

            const geminiResponse = await fetchAnyUrl(API_BASE + "/gemini/askPrompt/" + savedPrompt.id, true);
            console.log("Gemini response:", geminiResponse);
            return geminiResponse.jsonResponse;
        } else {
            console.log("Saved prompt has no ID:", savedPrompt);
        }

    } catch(err){
        console.log("could not send prompt to gemini, error: " + err)
    }
}

async function getPromptsByProjectId(projectId){
    const getPromptsApi = API_BASE + "/gemini/" + projectId;
    try{
        const prompts = await fetchAnyUrl(getPromptsApi, true)
        if(prompts) {
            return prompts;
        }
        else {
            return null;
        }
    } catch(err){
        console.log("could not fetch prompts, error: " + err)
    }
}



async function deleteProject(proj){
    const projectDeleted = `\n ${proj.projectName} \n ${proj.projectDescription} \n ${proj.id}`;
    const deleteProjectApi = API_BASE + "/projects/" + proj.id;

    try{
        const delProject = await postObjectAsJson(deleteProjectApi, proj, "DELETE", true);
        if(!delProject.ok){
            const proMsg = await delProject.text().catch(() => '');
            throw new Error(proMsg)
        }
        alert("deleted the following project: " + projectDeleted);
        await renderProjects();
    } catch(err){
        alert("could not deleted the following project: " + projectDeleted + "\n due to this error: " + err)
    }
}

// Optional: highlight nav links (Profile/Projects/Explore)
const navItems = document.querySelectorAll(".nav-item");
navItems.forEach(item => {
    item.addEventListener("click", () => {
        navItems.forEach(i => i.classList.remove("active"));
        item.classList.add("active");
    });
});


async function createProject(){
    console.log("creating project start")
    const createProjectApi = API_BASE + "/projects"


    const projectName = document.getElementById("projectName").value;
    const projectDescription = document.getElementById("projectDescription").value;

    const newProject = {
        projectName: projectName,
        projectDescription: projectDescription
    }
    try{
        let response = await postObjectAsJson(createProjectApi, newProject, "POST", true)
        console.log(response);
        console.log(response.status)
        if(response.status === 201){
            alert("Project has been created")
        } else {
            alert("an error happened, statuscode: " + response.status)
        }
    } catch(err){
        console.error("Error creating project: " + err);
        //alert("failed to create project" + err)
    }
    await renderProjects();
    form.reset();
}


