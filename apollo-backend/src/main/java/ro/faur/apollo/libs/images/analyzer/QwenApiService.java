package ro.faur.apollo.libs.images.analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ro.faur.apollo.libs.images.analyzer.dtos.QwenRequest;
import ro.faur.apollo.libs.images.analyzer.dtos.QwenResponse;

@Service
public class QwenApiService {

    private static final Logger logger = LoggerFactory.getLogger(QwenApiService.class);
    private static final String API_URL = "http://localhost:1234/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public QwenApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String getDescriptionFromImage(String base64Image) {
        try {
            QwenRequest request = new QwenRequest(base64Image);
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
                logger.info("Received Qwen API Response: {}", objectMapper.writeValueAsString(response.getBody()));
                return response.getBody().getMessageContent();
            } else {
                return "No description available";
            }
        } catch (Exception e) {
            logger.error("Error processing image description request", e);
            return "Error processing image";
        }
    }
}
