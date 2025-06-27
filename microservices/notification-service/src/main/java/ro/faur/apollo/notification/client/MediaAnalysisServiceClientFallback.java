package ro.faur.apollo.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class MediaAnalysisServiceClientFallback implements FallbackFactory<MediaAnalysisServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(MediaAnalysisServiceClientFallback.class);

    @Override
    public MediaAnalysisServiceClient create(Throwable cause) {
        return new MediaAnalysisServiceClient() {
            @Override
            public String analyzeMedia(String mediaUrl) {
                logger.error("MediaAnalysisServiceClient.analyzeMedia({}) failed - falling back to default message", mediaUrl, cause);
                return "Media analysis service is currently unavailable";
            }
        };
    }
} 