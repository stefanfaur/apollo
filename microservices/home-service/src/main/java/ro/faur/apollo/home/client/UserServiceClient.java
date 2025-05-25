package ro.faur.apollo.home.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ro.faur.apollo.shared.dto.UserDTO;

import java.util.List;

@FeignClient(name = "user-service", url = "${services.user.url:http://localhost:8087}")
public interface UserServiceClient {

    @GetMapping("/api/users/search")
    List<UserDTO> searchUsers(@RequestParam String email,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/users/{uuid}")
    UserDTO getUserByUuid(@PathVariable String uuid);

    @GetMapping("/api/users/email/{email}")
    UserDTO getUserByEmail(@PathVariable String email);
} 