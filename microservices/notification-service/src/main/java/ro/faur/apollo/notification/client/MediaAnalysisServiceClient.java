package ro.faur.apollo.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "media-analysis-service",
    url = "${services.media-analysis.url:}",
    fallback = MediaAnalysisServiceClientFallback.class
)
@Primary
public interface MediaAnalysisServiceClient {

    @PostMapping("/api/media/analyze")
    String analyzeMedia(@RequestParam String mediaUrl);
} 