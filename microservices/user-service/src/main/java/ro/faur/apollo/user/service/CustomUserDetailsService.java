package ro.faur.apollo.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ro.faur.apollo.user.domain.User;
import ro.faur.apollo.user.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getGoogleId() != null) {
            // OAuth2 user
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password("") // password not needed for OAuth2 users
                    .roles(user.getRoles().split(","))
                    .build();
        } else if (user.getPassword() != null) {
            // Regular user
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRoles().split(","))
                    .build();
        } else {
            throw new UsernameNotFoundException("User credentials are invalid.");
        }
    }
} 