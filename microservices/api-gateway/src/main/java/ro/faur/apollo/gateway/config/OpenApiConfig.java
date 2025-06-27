package ro.faur.apollo.gateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    @Lazy(false)
    public List<GroupedOpenApi> apis(SwaggerUiConfigParameters swaggerUiConfigParameters, RouteDefinitionLocator locator) {
        List<GroupedOpenApi> groups = new ArrayList<>();
        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
        
        // Add API Gateway group
        groups.add(GroupedOpenApi.builder()
                .pathsToMatch("/api/gateway/**")
                .group("api-gateway")
                .build());
        
        // Add microservices groups dynamically based on routes
        if (definitions != null) {
            definitions.stream()
                    .filter(routeDefinition -> routeDefinition.getId().endsWith("-openapi"))
                    .forEach(routeDefinition -> {
                        String name = routeDefinition.getId().replace("-openapi", "");
                        swaggerUiConfigParameters.addGroup(name);
                        GroupedOpenApi.builder()
                                .pathsToMatch("/api/" + name + "/**")
                                .group(name)
                                .build();
                    });
        }
        
        // Add static groups for our services
        swaggerUiConfigParameters.addGroup("user-service");
        swaggerUiConfigParameters.addGroup("device-service");
        swaggerUiConfigParameters.addGroup("media-analysis-service");
        swaggerUiConfigParameters.addGroup("home-service");
        swaggerUiConfigParameters.addGroup("notification-service");
        swaggerUiConfigParameters.addGroup("file-storage-service");
        
        return groups;
    }
} 