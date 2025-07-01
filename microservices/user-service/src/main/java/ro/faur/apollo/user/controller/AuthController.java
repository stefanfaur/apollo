package ro.faur.apollo.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.shared.dto.UserDTO;
import ro.faur.apollo.user.dto.JwtResponse;
import ro.faur.apollo.user.dto.LoginRequest;
import ro.faur.apollo.user.dto.RegisterRequest;
import ro.faur.apollo.user.service.AuthService;
import ro.faur.apollo.user.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = authService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unable to authenticate user: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            UserDTO user = userService.createUser(
                registerRequest.getUsername(), 
                registerRequest.getEmail(), 
                registerRequest.getPassword()
            );
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration");
        }
    }

    @PostMapping("/oauth2/login/google")
    public ResponseEntity<?> oauth2Login(@RequestParam("token") String googleToken) {
        log.info("Google OAuth2 login request received");
        log.debug("Request parameter 'token' present: {}", googleToken != null && !googleToken.trim().isEmpty());
        
        try {
            if (googleToken == null || googleToken.trim().isEmpty()) {
                log.warn("Google OAuth2 login attempt with empty or null token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Google token is required");
            }
            
            log.debug("Calling authService.authenticateWithGoogle()");
            String jwt = authService.authenticateWithGoogle(googleToken);
            log.info("Google OAuth2 login successful, returning JWT response");
            return ResponseEntity.ok(new JwtResponse(jwt));
            
        } catch (IllegalArgumentException e) {
            log.warn("Google OAuth2 login failed - Invalid token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Google token: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Google OAuth2 login failed - Runtime error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication service error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Google OAuth2 login error - Unexpected exception", e);
            log.error("Exception class: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage());
            if (e.getCause() != null) {
                log.error("Root cause: {}: {}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google OAuth authentication failed");
        }
    }
} 