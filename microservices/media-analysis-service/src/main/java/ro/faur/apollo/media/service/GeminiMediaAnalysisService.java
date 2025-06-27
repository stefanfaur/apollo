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
                "You are analyzing video from a smart home doorlock camera. " +
                        "Output only concise plain text with line breaks — no markdown, no lists. " +
                        "Do not describe the environment or background unless it changes. " +
                        "Focus only on unusual or new events involving people or motion.\n" +
                        "\n" +
                        "For each event, include:\n" +
                        "- Timestamp (MM:SS)\n" +
                        "- What person is doing (e.g. approaching, loitering, taking a package)\n" +
                        "- Who they appear to be (courier with identifiable logo/uniform, visitor, stranger)\n" +
                        "- Demeanor (friendly, suspicious, aggressive)\n" +
                        "- Any tools/weapons or suspicious objects\n" +
                        "- End with a short alert (e.g. \"ALERT: possible intruder\", \"ALERT: package theft\") **only if a security concern exists**.\n" +
                        "\n" +
                        "Use clean, short sentences separated by newlines. No extra narration. Be brief and informative like a notification.\n",
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