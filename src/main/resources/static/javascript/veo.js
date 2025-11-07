import fs from "fs";
import path from "path";
import { GoogleGenAI } from "@google/genai";

const ai = new GoogleGenAI({ apiKey: process.env.GOOGLE_API_KEY });

async function generateVideo(promptDone, videoTitle, videoId) {
    const prompt = `Make me a video about a guy called gustavo rock. He is white, 22 years old and goes to the gym. He loves white monster and gaming`;
    const donePromt = promptDone;

    // 1. Generate video
    let operation = await ai.models.generateVideos({
        model: "veo-3.1-generate-preview",
        prompt: prompt,
    });

    while (!operation.done) {
        console.log("Waiting for video generation to complete...");
        await new Promise((resolve) => setTimeout(resolve, 10000));
        operation = await ai.operations.getVideosOperation({
            operation: operation,
        });
    }

    // 2. Ensure videos folder exists
    const videosFolder = path.join(process.cwd(), "videos");
    if (!fs.existsSync(videosFolder)) {
        fs.mkdirSync(videosFolder);
    }

    // 3. Set custom video filename
    const videoName = "gustavo_rock.mp4"; // <-- variable name you choose
    const videoNameDone = videoTitle + "id" + videoId + ".mp4"
    const videoPath = path.join(videosFolder, videoName);

    // 4. Download video
    await ai.files.download({
        file: operation.response.generatedVideos[0].video,
        downloadPath: videoPath,
    });

    console.log(`✅ Generated video saved to ${videoPath}`);
}

generateVideo().catch(err => console.error(err));

export {generateVideo}