package ro.faur.apollo.user.controller;

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
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
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration");
        }
    }

    @PostMapping("/oauth2/login/google")
    public ResponseEntity<?> oauth2Login(@RequestParam("token") String googleToken) {
        try {
            String jwt = authService.authenticateWithGoogle(googleToken);
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token");
        }
    }
} 