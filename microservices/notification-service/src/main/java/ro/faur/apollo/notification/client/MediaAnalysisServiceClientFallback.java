package ro.faur.apollo.notification.client;

import org.springframework.stereotype.Component;

@Component
public class MediaAnalysisServiceClientFallback implements MediaAnalysisServiceClient {

    @Override
    public String analyzeMedia(String mediaUrl) {
        // Fallback: return a default message when media analysis service is unavailable
        return "Media analysis service is currently unavailable";
    }
} 