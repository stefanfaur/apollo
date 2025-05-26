package ro.faur.apollo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.faur.apollo.gateway.filter.JwtAuthenticationFilter;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${spring.cloud.kubernetes.enabled:false}")
    private boolean kubernetesEnabled;
    
    @Value("${services.user.url:http://localhost:8087}")
    private String userServiceUrl;
    
    @Value("${services.device.url:http://localhost:8082}")
    private String deviceServiceUrl;
    
    @Value("${services.media-analysis.url:http://localhost:8083}")
    private String mediaAnalysisServiceUrl;
    
    @Value("${services.home.url:http://localhost:8084}")
    private String homeServiceUrl;
    
    @Value("${services.notification.url:http://localhost:8085}")
    private String notificationServiceUrl;
    
    @Value("${services.file-storage.url:http://localhost:8086}")
    private String fileStorageServiceUrl;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service routes (auth endpoints - public)
                .route("user-auth", r -> r.path("/api/auth/**")
                        .uri(getUserServiceUri()))
                
                // User Service routes (protected)
                .route("user-protected", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(getUserServiceUri()))
                
                // Device Service routes (protected)
                .route("device-service", r -> r.path("/api/devices/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(getDeviceServiceUri()))
                
                // Media Analysis Service routes (protected)
                .route("media-analysis-service", r -> r.path("/api/media/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(getMediaAnalysisServiceUri()))
                
                // Home Service routes (protected)
                .route("home-service", r -> r.path("/api/home/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(getHomeServiceUri()))
                
                // Notification Service routes (protected)
                .route("notification-service", r -> r.path("/api/notification/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(getNotificationServiceUri()))
                
                // File Storage Service routes - some public, some protected
                .route("file-storage-public", r -> r.path("/api/files/presigned-url", "/api/files/convert-to-base64")
                        .uri(getFileStorageServiceUri()))
                
                .route("file-storage-protected", r -> r.path("/api/files/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(getFileStorageServiceUri()))
                
                // Legacy Minio routes
                .route("minio-legacy", r -> r.path("/api/minio/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(getFileStorageServiceUri()))
                
                .build();
    }
    
    private String getUserServiceUri() {
        return kubernetesEnabled ? "lb://user-service" : userServiceUrl;
    }
    
    private String getDeviceServiceUri() {
        return kubernetesEnabled ? "lb://device-service" : deviceServiceUrl;
    }
    
    private String getMediaAnalysisServiceUri() {
        return kubernetesEnabled ? "lb://media-analysis-service" : mediaAnalysisServiceUrl;
    }
    
    private String getHomeServiceUri() {
        return kubernetesEnabled ? "lb://home-service" : homeServiceUrl;
    }
    
    private String getNotificationServiceUri() {
        return kubernetesEnabled ? "lb://notification-service" : notificationServiceUrl;
    }
    
    private String getFileStorageServiceUri() {
        return kubernetesEnabled ? "lb://file-storage-service" : fileStorageServiceUrl;
    }
} 