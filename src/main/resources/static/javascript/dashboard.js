import {postObjectAsJson, fetchAnyUrl} from "./modulejson.js";
import {openVideoGenerationModal, openVideoPlayerModal} from "./veoScript.js";

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
    const getProjects = API_BASE + "/projects/me";
    const token = localStorage.getItem("jwt");

    if (!token) {
        console.error("No JWT token found!");
        return;
    }

    const projects = await fetchAnyUrl(getProjects, true);

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
        const projectItem = document.createElement("div");
        projectItem.classList.add("project-item");

        projectItem.innerHTML = `
            <h3>${proj.projectName}</h3>
            <p>${proj.projectDescription}</p>
            <button class="delete-btn">Delete</button>
        `;

        projectItem.addEventListener("click", (e) => {
            if (e.target.classList.contains("delete-btn")) return;
            openProject(proj);
        });

        const deleteBtn = projectItem.querySelector(".delete-btn");
        deleteBtn.addEventListener("click", (e) => {
            e.stopPropagation(); // Prevent opening project

            // Show confirmation dialog
            const confirmDelete = confirm(
                `Are you sure you want to delete "${proj.projectName}"?\n\nThis action cannot be undone.`
            );

            if (confirmDelete) {
                deleteProject(proj);
            }
        });
        projectsList.appendChild(projectItem);
    });
}

async function openProject(proj){
    document.getElementById("create-project").style.display = "none";
    document.getElementById("projects-section").style.display = "none";

    const detailsSection = document.getElementById("projectDetails");
    detailsSection.className = "project-view";
    detailsSection.style.display = "block";
    detailsSection.innerHTML = "";

    if(!detailsSection){
        return;
    }

    const backButton = document.createElement("button");
    backButton.textContent = "← Back"
    backButton.className = "back-btn";
    backButton.addEventListener("click", () => {
        document.getElementById("create-project").style.display = "block";
        document.getElementById("projects-section").style.display = "block";
        detailsSection.style.display = "none";
    });

    const title = document.createElement("h2");
    title.textContent = proj.projectName;

    const description = document.createElement("p");
    description.textContent = proj.projectDescription;

    const promptLabel = document.createElement("label");
    promptLabel.textContent = "Generate Video Prompt";

    const promptInput = document.createElement("textarea");
    promptInput.placeholder = "Enter idea for prompt..";

    const submitBtn = document.createElement("button");
    submitBtn.textContent = "Submit prompt";

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

    responseBox.className = "response-box";
    detailsSection.appendChild(responseBox);

    // Load earlier prompts for this project
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
        oldPromptReceived.readOnly = true;

        // ADD VIDEO GENERATION BUTTON

        //knappen bliver enten til en generate video eller
        let genOrWatchVidBtn;
        if(singlePrompt.video === null || singlePrompt.video === undefined) {
            // No video exists - show generate button
            genOrWatchVidBtn = document.createElement("button");
            genOrWatchVidBtn.textContent = "🎬 Generate Video";
            genOrWatchVidBtn.className = "generate-video-btn";
            genOrWatchVidBtn.addEventListener("click", () => {
                openVideoGenerationModal(singlePrompt.jsonResponse, singlePrompt.id);
            });
        } else {
            // Video exists - show watch button
            genOrWatchVidBtn = document.createElement("button");
            genOrWatchVidBtn.textContent = "▶️ Watch Video";
            genOrWatchVidBtn.className = "watch-video-btn";
            genOrWatchVidBtn.addEventListener("click", () => {
                openVideoPlayerModal(singlePrompt.video);
            });
        }

        oldPromptBox.appendChild(oldPromptSend);
        oldPromptBox.appendChild(oldPromptReceived);
        oldPromptBox.appendChild(genOrWatchVidBtn);
        oldPromptsContainer.appendChild(oldPromptBox);
    })

    detailsSection.appendChild(oldPromptsContainer);
}

function loadDashboardView() {
    location.reload();
}

async function sendPromptToGemini(prompt, projectId){
    const savePromptInDB = API_BASE + "/gemini/makePrompt";
    const geminiMessageDto = { prompt, projectId };

    try {
        const response = await postObjectAsJson(savePromptInDB, geminiMessageDto, "POST", true);

        const savedPrompt = await response.json?.() ?? response;

        if(savedPrompt && savedPrompt.id){

            const geminiResponse = await fetchAnyUrl(API_BASE + "/gemini/askPrompt/" + savedPrompt.id, true);
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
        await renderProjects();
    } catch(err){
        alert("could not deleted the following project: " + projectDeleted + "\n due to this error: " + err)
    }
}

const navItems = document.querySelectorAll(".nav-item");
navItems.forEach(item => {
    item.addEventListener("click", () => {
        navItems.forEach(i => i.classList.remove("active"));
        item.classList.add("active");
    });
});

async function createProject(){
    const createProjectApi = API_BASE + "/projects"

    const projectName = document.getElementById("projectName").value;
    const projectDescription = document.getElementById("projectDescription").value;

    const newProject = {
        projectName: projectName,
        projectDescription: projectDescription
    }
    try{
        let response = await postObjectAsJson(createProjectApi, newProject, "POST", true)
        if(response.status === 201){
            alert("Project has been created")
        } else {
            alert("an error happened, statuscode: " + response.status)
        }
    } catch(err){
        console.error("Error creating project: " + err);
    }
    await renderProjects();
    form.reset();
}