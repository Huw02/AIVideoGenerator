const API_BASE_URL = 'http://localhost:8080/api/v1/videos';

let currentVideoId = null;
let pollingInterval = null;

const promptInput = document.getElementById('promptInput');
const generateBtn = document.getElementById('generateBtn');
const clearBtn = document.getElementById('clearBtn');
const statusDiv = document.getElementById('status');
const videoInfoDiv = document.getElementById('videoInfo');
const progressBar = document.getElementById('progressBar');
const progressFill = document.getElementById('progressFill');

// Generate video from prompt
async function generateVideo(prompt) {
    const response = await fetch(`${API_BASE_URL}/generate`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ prompt: prompt })
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data;
}

// Check video status - using query parameter instead of path variable
async function checkVideoStatus(operationName) {
    const encodedOperation = encodeURIComponent(operationName);
    const response = await fetch(`${API_BASE_URL}/status?operationName=${encodedOperation}`);

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data;
}

// Download and save video - using query parameter instead of path variable
async function downloadVideo(operationName) {
    const encodedOperation = encodeURIComponent(operationName);
    const response = await fetch(`${API_BASE_URL}/download?operationName=${encodedOperation}`, {
        method: 'POST'
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data;
}

// Update status display
function updateStatus(message, type = 'info', showSpinner = false) {
    statusDiv.className = `status-content ${type}`;
    statusDiv.innerHTML = showSpinner
        ? `<span class="loading-spinner"></span>${message}`
        : message;
}

// Update progress bar
function updateProgress(percent) {
    if (percent > 0) {
        progressBar.style.display = 'block';
        progressFill.style.width = `${percent}%`;
    } else {
        progressBar.style.display = 'none';
    }
}

// Display video info
function displayVideoInfo(operationName, status, videoUrl = null) {
    videoInfoDiv.style.display = 'block';

    // Truncate operation name for display
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
async function pollVideoStatus(operationName) {
    let attempts = 0;
    const maxAttempts = 120; // 10 minutes (5 seconds * 120)

    pollingInterval = setInterval(async () => {
        try {
            attempts++;
            const progress = Math.min((attempts / maxAttempts) * 100, 90);
            updateProgress(progress);

            const statusResponse = await checkVideoStatus(operationName);

            updateStatus(
                `Checking video status... (Attempt ${attempts}/${maxAttempts})`,
                'processing',
                true
            );

            displayVideoInfo(operationName, statusResponse.status, statusResponse.videoUrl);

            if (statusResponse.status === 'COMPLETED') {
                clearInterval(pollingInterval);
                updateStatus('Video completed! Downloading...', 'processing', true);
                updateProgress(95);

                const downloadResponse = await downloadVideo(operationName);
                updateProgress(100);

                setTimeout(() => {
                    updateStatus('✅ Video generated and saved successfully!', 'success');
                    displayVideoInfo(operationName, 'DOWNLOADED', downloadResponse.videoUrl);
                    generateBtn.disabled = false;
                    updateProgress(0);
                }, 500);
            } else if (statusResponse.status === 'FAILED' || statusResponse.status === 'ERROR') {
                clearInterval(pollingInterval);
                updateStatus('❌ Video generation failed', 'error');
                generateBtn.disabled = false;
                updateProgress(0);
            } else if (attempts >= maxAttempts) {
                clearInterval(pollingInterval);
                updateStatus('⏱️ Timeout: Video is taking too long to generate', 'error');
                generateBtn.disabled = false;
                updateProgress(0);
            }
        } catch (error) {
            clearInterval(pollingInterval);
            updateStatus(`❌ Error: ${error.message}`, 'error');
            generateBtn.disabled = false;
            updateProgress(0);
        }
    }, 5000); // Check every 5 seconds
}

// Generate button click handler
generateBtn.addEventListener('click', async () => {
    const prompt = promptInput.value.trim();

    if (!prompt) {
        alert('Please enter a prompt');
        return;
    }

    try {
        generateBtn.disabled = true;
        videoInfoDiv.style.display = 'none';
        updateStatus('🚀 Initiating video generation...', 'processing', true);
        updateProgress(10);

        const result = await generateVideo(prompt);
        currentVideoId = result.videoId;

        updateStatus('✓ Video generation started! Waiting for completion...', 'processing', true);
        updateProgress(20);
        displayVideoInfo(result.videoId, result.status);

        // Start polling for status
        await pollVideoStatus(result.videoId);

    } catch (error) {
        updateStatus(`❌ Error: ${error.message}`, 'error');
        generateBtn.disabled = false;
        updateProgress(0);
        console.error('Error:', error);
    }
});

// Clear button click handler
clearBtn.addEventListener('click', () => {
    promptInput.value = '';
    statusDiv.className = 'status-content';
    statusDiv.textContent = 'Ready to generate video';
    videoInfoDiv.style.display = 'none';
    currentVideoId = null;
    updateProgress(0);

    if (pollingInterval) {
        clearInterval(pollingInterval);
    }

    generateBtn.disabled = false;
});

// Allow Ctrl+Enter in textarea to trigger generation
promptInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
        generateBtn.click();
    }
});

export {generateVideo, checkVideoStatus, downloadVideo, updateProgress, updateStatus, pollVideoStatus, displayVideoInfo}