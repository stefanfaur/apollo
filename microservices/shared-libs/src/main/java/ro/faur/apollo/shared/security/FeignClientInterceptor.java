package ro.faur.apollo.shared.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Feign client interceptor to automatically propagate JWT tokens between microservices.
 * Configured as a @Bean in FeignConfiguration.
 */
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getCredentials() instanceof String) {
            String token = (String) authentication.getCredentials();
            if (token != null && !token.isEmpty()) {
                template.header("Authorization", "Bearer " + token);
            }
        }
    }
} 