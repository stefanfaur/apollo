package ro.faur.apollo.notification.client;

import org.springframework.stereotype.Component;
import ro.faur.apollo.notification.dto.HomeDTO;
import ro.faur.apollo.shared.dto.HomeSummaryDTO;

import java.util.Collections;
import java.util.List;

@Component
public class HomeServiceClientFallback implements HomeServiceClient {

    @Override
    public List<HomeDTO> getHomesForUser(String userUuid) {
        // Fallback: return empty list when home service is unavailable
        return Collections.emptyList();
    }

    @Override
    public List<HomeSummaryDTO> getHomeSummariesForUser(String userUuid) {
        return Collections.emptyList();
    }
} 