package ro.faur.apollo.libs.auth.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.faur.apollo.libs.auth.jwt.JwtTokenProvider;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.auth.user.repository.UserRepository;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticateWithGoogle(String idTokenString) throws Exception {
        // verify Google ID token
        GoogleIdToken.Payload payload = GoogleOAuthTokenChecker.verifyToken(idTokenString);
        String email = payload.getEmail();
        String googleId = payload.getSubject(); // google user ID

        // find or create the user in the database
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email.split("@")[0]);
            newUser.setGoogleId(googleId);
            newUser.setRoles("USER");
            return userRepository.save(newUser);
        });

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword() != null ? user.getPassword() : "",
                user.getRoles() != null ? List.of(new SimpleGrantedAuthority(user.getRoles())) : List.of()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        return jwtTokenProvider.generateToken(authentication);
    }
}
