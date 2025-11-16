// veoScript.js - Video Generation Integration using existing modulejson functions
import { fetchAnyUrl, postObjectAsJson } from './modulejson.js';

const API_BASE_URL = 'http://localhost:8080/api/v1/videos';

let currentVideoId = null;
let pollingInterval = null;

// Initialize video generation when DOM is loaded
export function initVideoGeneration() {
    console.log("Video generation module initialized");
}

// Generate video from prompt
async function generateVideo(prompt) {
    const url = `${API_BASE_URL}/generate`;
    const payload = { prompt: prompt };

    const response = await postObjectAsJson(url, payload, "POST", true);

    if (!response || !response.ok) {
        throw new Error(`HTTP error! status: ${response?.status || 'unknown'}`);
    }

    const data = await response.json();
    return data;
}

// Check video status
async function checkVideoStatus(operationName) {
    const encodedOperation = encodeURIComponent(operationName);
    const url = `${API_BASE_URL}/status?operationName=${encodedOperation}`;

    const data = await fetchAnyUrl(url, true);

    if (!data) {
        throw new Error('Failed to fetch video status');
    }

    return data;
}

// Download and save video
async function downloadVideo(operationName) {
    const encodedOperation = encodeURIComponent(operationName);
    const url = `${API_BASE_URL}/download?operationName=${encodedOperation}`;

    const response = await postObjectAsJson(url, {}, "POST", true);

    if (!response || !response.ok) {
        throw new Error(`HTTP error! status: ${response?.status || 'unknown'}`);
    }

    const data = await response.json();
    return data;
}

// Update status display
function updateStatus(statusDiv, message, type = 'info', showSpinner = false) {
    statusDiv.className = `status-content ${type}`;
    statusDiv.innerHTML = showSpinner
        ? `<span class="loading-spinner"></span>${message}`
        : message;
}

// Update progress bar
function updateProgress(progressBar, progressFill, percent) {
    if (percent > 0) {
        progressBar.style.display = 'block';
        progressFill.style.width = `${percent}%`;
    } else {
        progressBar.style.display = 'none';
    }
}

// Display video info
function displayVideoInfo(videoInfoDiv, operationName, status, videoUrl = null) {
    videoInfoDiv.style.display = 'block';

    const displayName = operationName.length > 50
        ? operationName.substring(0, 47) + '...'
        : operationName;

    videoInfoDiv.innerHTML = `
        <div class="video-info">
            <div class="video-info-item">
                <span class="video-info-label">Operation:</span>
                <span class="video-info-value" title="${operationName}">${displayName}</span>
            </div>
            <div class="video-info-item">
                <span class="video-info-label">Status:</span>
                <span class="video-info-value">${status}</span>
            </div>
            ${videoUrl ? `
                <div class="video-info-item">
                    <span class="video-info-label">File Path:</span>
                    <span class="video-info-value">${videoUrl}</span>
                </div>
            ` : ''}
        </div>
    `;
}

// Poll video status
async function pollVideoStatus(operationName, statusDiv, progressBar, progressFill, videoInfoDiv, generateBtn) {
    let attempts = 0;
    const maxAttempts = 120; // 10 minutes

    pollingInterval = setInterval(async () => {
        try {
            attempts++;
            const progress = Math.min((attempts / maxAttempts) * 100, 90);
            updateProgress(progressBar, progressFill, progress);

            const statusResponse = await checkVideoStatus(operationName);

            updateStatus(
                statusDiv,
                `Checking video status... (Attempt ${attempts}/${maxAttempts})`,
                'processing',
                true
            );

            displayVideoInfo(videoInfoDiv, operationName, statusResponse.status, statusResponse.videoUrl);

            if (statusResponse.status === 'COMPLETED') {
                clearInterval(pollingInterval);
                updateStatus(statusDiv, 'Video completed! Downloading...', 'processing', true);
                updateProgress(progressBar, progressFill, 95);

                const downloadResponse = await downloadVideo(operationName);
                updateProgress(progressBar, progressFill, 100);

                setTimeout(() => {
                    updateStatus(statusDiv, '✅ Video generated and saved successfully!', 'success');
                    displayVideoInfo(videoInfoDiv, operationName, 'DOWNLOADED', downloadResponse.videoUrl);
                    generateBtn.disabled = false;
                    updateProgress(progressBar, progressFill, 0);
                }, 500);
                return true
            } else if (statusResponse.status === 'FAILED' || statusResponse.status === 'ERROR') {
                clearInterval(pollingInterval);
                updateStatus(statusDiv, '❌ Video generation failed', 'error');
                generateBtn.disabled = false;
                updateProgress(progressBar, progressFill, 0);
            } else if (attempts >= maxAttempts) {
                clearInterval(pollingInterval);
                updateStatus(statusDiv, '⏱️ Timeout: Video is taking too long to generate', 'error');
                generateBtn.disabled = false;
                updateProgress(progressBar, progressFill, 0);
            }
        } catch (error) {
            clearInterval(pollingInterval);
            updateStatus(statusDiv, `❌ Error: ${error.message}`, 'error');
            generateBtn.disabled = false;
            updateProgress(progressBar, progressFill, 0);
        }
    }, 5000);
}

// Main function to open video generation modal
export function openVideoGenerationModal(promptText, promptId) {
    // Check if user is logged in
    const token = localStorage.getItem("jwt");
    if (!token) {
        alert('Please log in to generate videos');
        return;
    }

    // Create modal overlay
    const modalOverlay = document.createElement('div');
    modalOverlay.className = 'veo-modal-overlay';
    modalOverlay.id = 'veoModalOverlay';

    // Create modal content
    modalOverlay.innerHTML = `
        <div class="veo-modal">
            <div class="veo-modal-header">
                <h2>🎬 Generate Video with Veo 3</h2>
                    <button class="veo-modal-close" id="closeVeoModal">✕</button>
            </div>
            
            <div class="veo-modal-body">
                <div class="input-group">
                    <label for="veoPromptInput">Video Prompt</label>
                    <textarea 
                        id="veoPromptInput" 
                        readonly
                        rows="4"
                    >${promptText}</textarea>
                </div>

                <div class="button-group">
                    <button id="veoGenerateBtn" class="btn-primary">Generate Video</button>
                    <button id="veoClearBtn" class="btn-secondary">Close</button>
                </div>

                <div class="status-panel">
                    <div class="status-title">Status</div>
                    <div id="veoStatus" class="status-content">Ready to generate video</div>
                    <div id="veoProgressBar" class="progress-bar" style="display: none;">
                        <div id="veoProgressFill" class="progress-fill" style="width: 0%"></div>
                    </div>
                    <div id="veoVideoInfo" style="display: none;"></div>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modalOverlay);

    // Get modal elements
    const closeBtn = document.getElementById('closeVeoModal');
    const generateBtn = document.getElementById('veoGenerateBtn');
    const clearBtn = document.getElementById('veoClearBtn');
    const promptInput = document.getElementById('veoPromptInput');
    const statusDiv = document.getElementById('veoStatus');
    const videoInfoDiv = document.getElementById('veoVideoInfo');
    const progressBar = document.getElementById('veoProgressBar');
    const progressFill = document.getElementById('veoProgressFill');

    // Close modal function
    const closeModal = () => {
        if (pollingInterval) {
            clearInterval(pollingInterval);
        }
        modalOverlay.remove();
    };

    // Event listeners
    closeBtn.addEventListener('click', closeModal);
    clearBtn.addEventListener('click', closeModal);

    // Close on overlay click
    modalOverlay.addEventListener('click', (e) => {
        if (e.target === modalOverlay) {
            closeModal();
        }
    });

    // Generate video button
    generateBtn.addEventListener('click', async () => {
        const prompt = promptInput.value.trim();

        if (!prompt) {
            alert('Prompt is empty!');
            return;
        }

        try {
            generateBtn.disabled = true;
            videoInfoDiv.style.display = 'none';
            updateStatus(statusDiv, '🚀 Initiating video generation...', 'processing', true);
            updateProgress(progressBar, progressFill, 10);

            const result = await generateVideo(prompt);
            currentVideoId = result.videoId;

            updateStatus(statusDiv, '✓ Video generation started! Waiting for completion...', 'processing', true);
            updateProgress(progressBar, progressFill, 20);
            displayVideoInfo(videoInfoDiv, result.videoId, result.status);

            // Start polling for status
            const pollVideoReponse = await pollVideoStatus(result.videoId, statusDiv, progressBar, progressFill, videoInfoDiv, generateBtn);
            console.log(pollVideoReponse)
            if(pollVideoReponse === true){
                const videoId = await getIdByVideoId(result.videoId);
                console.log(videoId)
                if(videoId !== null){
                await updateGeminimessageVideoId(promptId, videoId)
                    }
            } // blev nødt til at lave ovenstående, da en video både har id(en long/ PK) og videoId(en string)

        } catch (error) {
            updateStatus(statusDiv, `❌ Error: ${error.message}`, 'error');
            generateBtn.disabled = false;
            updateProgress(progressBar, progressFill, 0);
            console.error('Error:', error);
        }
    });
}

async function updateGeminimessageVideoId(geminimessageId, videoId){
    const postApiUrl = API_BASE_URL + "/updatedGeminiMessage";

    const objToUpdateDB = {
        "id": geminimessageId,
        "videoId": videoId
    }
    try{
        await postObjectAsJson(postApiUrl, objToUpdateDB, "POST", true);
        console.log("update gemini post er kaldt")
    } catch(err){
        console.log("an error has occured:" + err)
    }
}

async function getIdByVideoId(videoId){
    const postApiUrl = API_BASE_URL + "/updatedGeminiMessage/" + videoId;
    try{
        const response = await fetchAnyUrl(postApiUrl, true);
        if(response != null){
            console.log("get er kaldt, response var:" + response)
            return response;
        }
    } catch(err){
        console.log("error: " + err);
    }
}

function openVideoPlayerModal(video) {
    // Get JWT token from localStorage
    const token = localStorage.getItem("jwt");

    // Create modal overlay
    const modalOverlay = document.createElement('div');
    modalOverlay.className = 'veo-modal-overlay';
    modalOverlay.id = 'videoPlayerOverlay';

    // Construct the streaming URL with token as query parameter
    const videoStreamUrl = `http://localhost:8080/api/v1/videos/stream/${video.id}`;

    // Rest of your modal code with videoStreamUrl...
    modalOverlay.innerHTML = `
        <div class="veo-modal video-player-modal">
            <div class="veo-modal-header">
                <h2>🎬 Video Player</h2>
            </div>
            
            <div class="veo-modal-body">
                <div class="video-container">
                    <video controls width="100%" id="videoElement">
                        <source src="${videoStreamUrl}" type="video/mp4">
                        Your browser does not support the video tag.
                    </video>
                </div>
                
                <div class="video-details">
                    
                    
                    ${video.createdAt ? `
                        <div class="video-info-item">
                            <span class="video-info-label">Created:</span>
                            <span class="video-info-value">${new Date(video.createdAt).toLocaleString()}</span>
                        </div>
                    ` : ''}
                </div>

                <div class="button-group">
                    <button id="closeVideoBtn" class="btn-secondary">Close</button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modalOverlay);

    // Event listeners...
    const closeVideoBtn = document.getElementById('closeVideoBtn');
    const videoElement = document.getElementById('videoElement');

    const closeModal = () => {
        videoElement.pause();
        modalOverlay.remove();
    };

    closeVideoBtn.addEventListener('click', closeModal);



    modalOverlay.addEventListener('click', (e) => {
        if (e.target === modalOverlay) {
            closeModal();
        }
    });
}

// Export for use in other modules
export { generateVideo, checkVideoStatus, downloadVideo, openVideoPlayerModal};