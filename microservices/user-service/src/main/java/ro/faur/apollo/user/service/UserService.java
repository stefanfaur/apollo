package ro.faur.apollo.user.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.faur.apollo.shared.dto.UserDTO;
import ro.faur.apollo.shared.security.UserContext;
import ro.faur.apollo.user.domain.User;
import ro.faur.apollo.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserContext userContext;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserContext userContext) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userContext = userContext;
    }

    public UserDTO createUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles("USER");

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO createOAuthUser(String username, String email, String googleId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }
            return convertToDTO(user);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setRoles("USER");

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    public List<UserDTO> searchUsersByEmail(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByEmailContainingIgnoreCase(email, pageable)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> findByUuid(String uuid) {
        return userRepository.findById(uuid)
                .map(this::convertToDTO);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getUuid(), user.getUsername(), user.getEmail(), user.getRoles());
    }

    public Optional<UserDTO> getCurrentUser() {
        String currentUsername = userContext.getCurrentUsername();
        if (currentUsername == null) {
            return Optional.empty();
        }
        return findByUsername(currentUsername);
    }
}