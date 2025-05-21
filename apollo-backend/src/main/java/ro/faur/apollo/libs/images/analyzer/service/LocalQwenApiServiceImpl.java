package ro.faur.apollo.libs.images.analyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ro.faur.apollo.libs.images.analyzer.dtos.QwenRequest;
import ro.faur.apollo.libs.images.analyzer.dtos.QwenResponse;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "qwen.service", havingValue = "local")
public class LocalQwenApiServiceImpl implements MediaAnalysisApiService {

    private static final Logger logger = LoggerFactory.getLogger(LocalQwenApiServiceImpl.class);
    private static final String API_URL = "http://localhost:1234/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LocalQwenApiServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getDescriptionFromImage(String base64Image) {
        try {
            QwenRequest request = new QwenRequest(base64Image);
            addSystemPromptToRequest(request, "image");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            String requestJson = objectMapper.writeValueAsString(request);

            HttpEntity<QwenRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<QwenResponse> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    QwenResponse.class
            );

            if (response.getBody() != null) {
                return response.getBody().getMessageContent();
            } else {
                return "No description available";
            }
        } catch (Exception e) {
            logger.error("Error processing image description request", e);
            return "Error processing image";
        }
    }

    @Override
    public String getDescriptionFromVideo(String base64Video) {
        try {
            QwenRequest request = new QwenRequest(base64Video);
            addSystemPromptToRequest(request, "video");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            String requestJson = objectMapper.writeValueAsString(request);

            HttpEntity<QwenRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<QwenResponse> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    QwenResponse.class
            );

            if (response.getBody() != null) {
                return response.getBody().getMessageContent();
            } else {
                return "No description available";
            }
        } catch (Exception e) {
            logger.error("Error processing video description request", e);
            return "Error processing video";
        }
    }

    private void addSystemPromptToRequest(QwenRequest request, String mediaType) {
        List<QwenRequest.Message> messages = new ArrayList<>(request.getMessages());

        if (!messages.isEmpty()) {
            QwenRequest.Message originalMessage = messages.get(0);

            QwenRequest.Message newMessage = new QwenRequest.Message(
                    "user",
                    List.of(
                            new QwenRequest.Content("text", "You are analyzing a " + mediaType + " taken by an entrance door camera. Describe what you see. " +
                                    "Address the user directly telling them what is at their door. " +
                                    "Short and concise, 2 sentences. " +
                                    "If you identify security risks(masked people, violence, weapons), specify them. Masks and weapons are a clear threat."),
                            originalMessage.getContent().get(1) // Keep the original media content
                    )
            );

            messages.set(0, newMessage);
        }
        request.setMessages(messages);
    }
}
