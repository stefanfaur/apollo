package ro.faur.apollo.libs.auth.user.controller;

import io.micrometer.core.annotation.Timed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.libs.auth.jwt.JwtTokenProvider;
import ro.faur.apollo.libs.auth.jwt.dtos.JwtResponse;
import ro.faur.apollo.libs.auth.jwt.dtos.LoginRequest;
import ro.faur.apollo.libs.auth.jwt.dtos.RegisterRequest;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.auth.user.repository.UserRepository;
import ro.faur.apollo.libs.auth.user.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserRepository userRepository, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @PostMapping("/login")
    @Timed(value = "login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            String token = jwtTokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    // TODO: extract all this logic into a service
    @PostMapping("/register")
    @Timed(value = "register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // check if username is already taken
            if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("Username is already taken");
            }
            if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email is already taken");
            }

            // create a new user
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(new BCryptPasswordEncoder().encode(registerRequest.getPassword()));
            user.setEmail(registerRequest.getEmail());
            user.setRoles("USER");

            userRepository.save(user);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration");
        }
    }

    @PostMapping("/oauth2/login/google")
    @Timed(value = "oauth2Login")
    public ResponseEntity<?> oauth2Login(@RequestParam("token") String googleToken) {
        try {
            String jwt = authService.authenticateWithGoogle(googleToken);
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token");
        }
    }


}
