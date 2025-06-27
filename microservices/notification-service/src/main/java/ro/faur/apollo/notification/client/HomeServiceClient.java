package ro.faur.apollo.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ro.faur.apollo.notification.dto.HomeDTO;
import ro.faur.apollo.shared.dto.HomeSummaryDTO;

import java.util.List;

@FeignClient(
    name = "home-service",
    url = "${services.home.url:}",
    fallback = HomeServiceClientFallback.class
)
public interface HomeServiceClient {

    @GetMapping("/api/home/user/{userUuid}")
    List<HomeDTO> getHomesForUser(@PathVariable String userUuid);

    @GetMapping("/api/home/summary/user/{userUuid}")
    List<HomeSummaryDTO> getHomeSummariesForUser(@PathVariable String userUuid);
} 