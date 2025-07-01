package ro.faur.apollo.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleOAuthTokenChecker {
    
    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthTokenChecker.class);
    
    @Value("${google.ios.client-id}")
    private String clientId;
    
    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    private void init() {
        log.info("Initializing GoogleOAuthTokenChecker");
        log.debug("Google iOS Client ID configured: {}", clientId != null ? "Present" : "NULL");
        
        if (clientId == null || clientId.trim().isEmpty()) {
            log.error("CRITICAL: google.ios.client-id is not configured! OAuth will fail!");
            throw new IllegalStateException("google.ios.client-id must be configured for Google OAuth to work");
        }
        
        try {
            verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
            log.info("GoogleIdTokenVerifier initialized successfully with client ID: {}", 
                    clientId.substring(0, Math.min(10, clientId.length())) + "...");
        } catch (Exception e) {
            log.error("Failed to initialize GoogleIdTokenVerifier", e);
            throw new IllegalStateException("Failed to initialize Google OAuth token verifier", e);
        }
    }

    // Changed from static to instance method to properly use Spring-injected dependencies
    public GoogleIdToken.Payload verifyToken(String idTokenString) throws Exception {
        log.debug("Starting Google ID token verification");
        log.debug("Token string: {}", idTokenString != null ? "Present (" + idTokenString.length() + " chars)" : "NULL");
        
        if (verifier == null) {
            log.error("CRITICAL: GoogleIdTokenVerifier not initialized! Component may not have been properly initialized.");
            throw new IllegalStateException("GoogleOAuthTokenChecker not properly initialized");
        }
        
        if (idTokenString == null || idTokenString.trim().isEmpty()) {
            log.error("ID token string is null or empty");
            throw new IllegalArgumentException("ID token cannot be null or empty");
        }
        
        try {
            log.debug("Calling Google token verification API");
            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                log.info("Google ID token verification successful");
                log.debug("Token issuer: {}", payload.getIssuer());
                log.debug("Token audience: {}", payload.getAudience());
                log.debug("Token subject: {}", payload.getSubject());
                log.debug("Token email: {}", payload.getEmail());
                log.debug("Token email verified: {}", payload.getEmailVerified());
                log.debug("Token expiration: {}", payload.getExpirationTimeSeconds());
                log.debug("Token issued at: {}", payload.getIssuedAtTimeSeconds());
                
                return payload;
            } else {
                log.error("Google ID token verification failed - token is invalid or expired");
                log.error("This could be due to:");
                log.error("1. Invalid token format");
                log.error("2. Token expired");
                log.error("3. Wrong audience (client ID mismatch)");
                log.error("4. Token not signed by Google");
                log.error("Current client ID: {}", clientId);
                throw new IllegalArgumentException("Invalid or expired Google ID token");
            }
        } catch (GeneralSecurityException e) {
            log.error("Security error during token verification", e);
            log.error("This usually indicates a problem with the token signature or crypto operations");
            throw new IllegalArgumentException("Token signature verification failed: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Network/IO error during token verification", e);
            log.error("This usually indicates network connectivity issues or problems reaching Google's servers");
            throw new RuntimeException("Network error during token verification: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during token verification", e);
            log.error("Error class: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("Unexpected error during token verification: " + e.getMessage(), e);
        }
    }
} 