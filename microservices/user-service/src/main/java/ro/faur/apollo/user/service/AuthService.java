package ro.faur.apollo.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ro.faur.apollo.shared.dto.UserDTO;
import ro.faur.apollo.shared.security.JwtTokenProvider;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final GoogleOAuthTokenChecker googleOAuthTokenChecker;

    public AuthService(AuthenticationManager authenticationManager, 
                      JwtTokenProvider jwtTokenProvider,
                      UserService userService,
                      GoogleOAuthTokenChecker googleOAuthTokenChecker) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.googleOAuthTokenChecker = googleOAuthTokenChecker;
    }

    public String authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        
        // Get user details to extract UUID
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDTO user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));
        
        return jwtTokenProvider.generateToken(user.getUsername(), user.getUuid());
    }

    public String authenticateWithGoogle(String googleToken) throws Exception {
        log.info("Starting Google OAuth authentication process");
        log.debug("Google token received: {}", googleToken != null ? "Present (" + googleToken.length() + " chars)" : "NULL");
        
        try {
            // Step 1: Verify the Google token
            log.debug("Step 1: Verifying Google ID token");
            GoogleIdToken.Payload payload = googleOAuthTokenChecker.verifyToken(googleToken);
            log.info("Google token verification successful");
            
            // Step 2: Extract user information
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleId = payload.getSubject();
            
            log.info("Extracted user info - Email: {}, Name: {}, GoogleId: {}", 
                    email, name, googleId != null ? "Present" : "NULL");
            
            if (email == null || email.trim().isEmpty()) {
                log.error("Email is null or empty from Google token payload");
                throw new IllegalArgumentException("Email is required but not found in Google token");
            }
            
            if (googleId == null || googleId.trim().isEmpty()) {
                log.error("Google ID is null or empty from token payload");
                throw new IllegalArgumentException("Google ID is required but not found in token");
            }
            
            // Step 3: Create or find user
            log.debug("Step 3: Creating/finding OAuth user");
            UserDTO user = userService.createOAuthUser(name, email, googleId);
            log.info("OAuth user created/found successfully: {}", user.getUuid());
            
            // Step 4: Generate JWT token
            log.debug("Step 4: Generating JWT token");
            String jwtToken = jwtTokenProvider.generateToken(user.getUsername(), user.getUuid());
            log.info("Google OAuth authentication completed successfully for user: {}", user.getUsername());
            
            return jwtToken;
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid Google token or missing required fields: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid Google token: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Google OAuth authentication", e);
            log.error("Error class: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage());
            if (e.getCause() != null) {
                log.error("Root cause: {}: {}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
            }
            throw new RuntimeException("Google OAuth authentication failed: " + e.getMessage(), e);
        }
    }
} 