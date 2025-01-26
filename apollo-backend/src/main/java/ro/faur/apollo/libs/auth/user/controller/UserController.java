package ro.faur.apollo.libs.auth.user.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.auth.user.domain.UserDTO;
import ro.faur.apollo.libs.auth.user.repository.UserRepository;
import ro.faur.apollo.libs.auth.utils.UserContext;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserContext userContext;

    public UserController(UserRepository userRepository, UserContext userContext) {
        this.userRepository = userRepository;
        this.userContext = userContext;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<User> users = userRepository.findByEmailContainingIgnoreCase(
            query,
            PageRequest.of(page, size)
        );

        List<UserDTO> userDTOs = users.stream()
            .map(user -> new UserDTO(user.getUuid(), user.getEmail()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/me")
    public UserDTO getUser() {
        return UserDTO.fromEntity(userContext.getUser());
    }
}
