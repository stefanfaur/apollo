package ro.faur.apollo.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.faur.apollo.gateway.filter.JwtAuthenticationFilter;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service routes (auth endpoints - public)
                .route("user-auth", r -> r.path("/api/auth/**")
                        .uri("http://localhost:8087"))
                
                // User Service routes (protected)
                .route("user-protected", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8087"))
                
                // Device Service routes (protected)
                .route("device-service", r -> r.path("/api/devices/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8082"))
                
                // Media Analysis Service routes (protected)
                .route("media-analysis-service", r -> r.path("/api/media/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8083"))
                
                // Home Service routes (protected)
                .route("home-service", r -> r.path("/api/home/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8084"))
                
                // Notification Service routes (protected)
                .route("notification-service", r -> r.path("/api/notification/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8085"))
                
                // File Storage Service routes - some public, some protected
                .route("file-storage-public", r -> r.path("/api/files/presigned-url", "/api/files/convert-to-base64")
                        .uri("http://localhost:8086"))
                
                .route("file-storage-protected", r -> r.path("/api/files/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8086"))
                
                // Legacy Minio routes
                .route("minio-legacy", r -> r.path("/api/minio/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8086"))
                
                .build();
    }
} 