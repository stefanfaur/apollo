package ro.faur.apollo.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ro.faur.apollo.shared.dto.UserDTO;
import ro.faur.apollo.shared.security.JwtTokenProvider;

@Service
public class AuthService {

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
        GoogleIdToken.Payload payload = GoogleOAuthTokenChecker.verifyToken(googleToken);
        
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String googleId = payload.getSubject();

        UserDTO user = userService.createOAuthUser(name, email, googleId);
        return jwtTokenProvider.generateToken(user.getUsername(), user.getUuid());
    }
} 