package ro.faur.apollo.libs.images.analyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
@ConditionalOnProperty(name = "qwen.service", havingValue = "external", matchIfMissing = true)
public class OpenrouterQwenApiServiceImpl implements QwenApiService {

    private static final Logger logger = LoggerFactory.getLogger(OpenrouterQwenApiServiceImpl.class);
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openrouter.apiKey}")
    private String openRouterApiKey;

    public OpenrouterQwenApiServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getDescriptionFromImage(String base64Image) {
        try {
            QwenRequest request = new QwenRequest(base64Image);
            request.setModel("qwen/qwen-2-vl-72b-instruct"); // Override model for OpenRouter
            addSystemPromptToRequest(request);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + openRouterApiKey);

            String requestJson = objectMapper.writeValueAsString(request);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
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

    private void addSystemPromptToRequest(QwenRequest request) {
        List<QwenRequest.Message> messages = new ArrayList<>(request.getMessages());

        if (!messages.isEmpty()) {
            QwenRequest.Message originalMessage = messages.get(0);

            QwenRequest.Message newMessage = new QwenRequest.Message(
                    "system",
                    List.of(
                            new QwenRequest.Content("text", "You are analyzing an image taken by an entrance door camera. Describe people you see. " +
                                    "Adress the user directly telling him what is at his door. " +
                                    "Short and concise, 2 sentences." +
                                    "If you identify security risks(masked people, violence, weapons), specify them. Masks and weapons are a clear threat."),
                            originalMessage.getContent().get(1) // Keep the original image content
                    )
            );

            messages.set(0, newMessage);
        }
        request.setMessages(messages);
    }

}
