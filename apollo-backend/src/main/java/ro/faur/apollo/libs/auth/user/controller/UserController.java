package ro.faur.apollo.libs.auth.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.faur.apollo.libs.auth.user.domain.UserDTO;
import ro.faur.apollo.libs.auth.utils.UserContext;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserContext userContext;

    public UserController(UserContext userContext) {
        this.userContext = userContext;
    }

    @GetMapping("/me")
    public UserDTO getUser() {
        return UserDTO.fromEntity(userContext.getUser());
    }
}
