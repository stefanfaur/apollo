package ro.faur.apollo.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ro.faur.apollo.notification.dto.HomeDTO;
import ro.faur.apollo.shared.dto.HomeSummaryDTO;

import java.util.Collections;
import java.util.List;

@Component
public class HomeServiceClientFallback implements FallbackFactory<HomeServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(HomeServiceClientFallback.class);

    @Override
    public HomeServiceClient create(Throwable cause) {
        return new HomeServiceClient() {
            @Override
            public List<HomeDTO> getHomesForUser(String userUuid) {
                logger.error("HomeServiceClient.getHomesForUser({}) failed - falling back to empty list", userUuid, cause);
                return Collections.emptyList();
            }

            @Override
            public List<HomeSummaryDTO> getHomeSummariesForUser(String userUuid) {
                logger.error("HomeServiceClient.getHomeSummariesForUser({}) failed - falling back to empty list", userUuid, cause);
                return Collections.emptyList();
            }
        };
    }
} 