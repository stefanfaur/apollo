package ro.faur.apollo.libs.images.analyzer.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@ConditionalOnProperty(name = "gemini.service", havingValue = "enabled", matchIfMissing = true)
public class GeminiApiServiceImpl implements MediaAnalysisApiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiApiServiceImpl.class);
    private static final String MODEL_ID = "gemini-2.0-flash-lite";

    private final Client client;

    public GeminiApiServiceImpl(@Value("${gemini.apiKey}") String apiKey) {
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
        return Content.fromParts(
            Part.fromBytes(mediaBytes, mimeType),
            Part.fromText("You are analyzing a " + mediaType + " taken by an entrance door camera. Describe what you see. " +
                    "Address the user directly telling them what is at their door. " +
                    "Short and concise, 2 sentences. " +
                    "If you identify security risks(masked people, violence, weapons), specify them. Masks and weapons are a clear threat.")
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