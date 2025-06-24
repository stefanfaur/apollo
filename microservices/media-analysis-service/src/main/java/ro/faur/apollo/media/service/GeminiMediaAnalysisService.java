package ro.faur.apollo.media.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class GeminiMediaAnalysisService implements MediaAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiMediaAnalysisService.class);
    private static final String MODEL_ID = "gemini-2.0-flash-lite";

    private final Client client;

    public GeminiMediaAnalysisService(@Value("${gemini.apiKey}") String apiKey) {
        this.client = Client.builder().apiKey(apiKey).build();
    }

    @Override
    public String getDescriptionFromImage(String base64Image) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            Content content = createContent(imageBytes, "image/jpeg", "image");
            return processRequest(content);
        } catch (Exception e) {
            logger.error("Error processing image description request", e);
            return "Error processing image";
        }
    }

    @Override
    public String getDescriptionFromVideo(String base64Video) {
        try {
            byte[] videoBytes = Base64.getDecoder().decode(base64Video);
            Content content = createContent(videoBytes, "video/mp4", "video");
            return processRequest(content);
        } catch (Exception e) {
            logger.error("Error processing video description request", e);
            return "Error processing video";
        }
    }

    private Content createContent(byte[] mediaBytes, String mimeType, String mediaType) {
        String prompt = String.format(
                "Analyze this %s from a doorlock security camera perspective. " +
                        "Focus on any potential security concerns. " +
                        "Very briefly describe people outside the door if they are present. " +
                        "Identify if there are intruders/weapons/suspicious activities. " +
                        "Keep the description very concise.",
                mediaType
        );

        return Content.fromParts(
            Part.fromBytes(mediaBytes, mimeType),
            Part.fromText(prompt)
        );
    }

    private String processRequest(Content content) {
        GenerateContentConfig config = GenerateContentConfig.builder()
            .responseMimeType("text/plain")
            .build();

        GenerateContentResponse response = client.models.generateContent(MODEL_ID, content, config);
        return response.text();
    }
} 