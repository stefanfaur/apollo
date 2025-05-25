package ro.faur.apollo.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility class for access to current user making the request in microservices.
 */
@Component
public class UserContext {

    private final JwtTokenProvider jwtTokenProvider;

    public UserContext(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String getCurrentUserUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getName())) {
            
            // Extract UUID from JWT token stored in credentials
            Object credentials = authentication.getCredentials();
            if (credentials instanceof String) {
                String jwtToken = (String) credentials;
                String uuid = jwtTokenProvider.extractUserUuid(jwtToken);
                if (uuid != null) {
                    return uuid;
                }
            }
            
            // Fallback to username if UUID not found in token
            return authentication.getName();
        }
        return null;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getName())) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                return ((UserDetails) authentication.getPrincipal()).getUsername();
            }
            return authentication.getName();
        }
        return null;
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && !"anonymousUser".equals(authentication.getName());
    }
} 