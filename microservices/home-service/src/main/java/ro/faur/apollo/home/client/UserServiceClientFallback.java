package ro.faur.apollo.home.client;

import org.springframework.stereotype.Component;
import ro.faur.apollo.shared.dto.UserDTO;

import java.util.Collections;
import java.util.List;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public List<UserDTO> searchUsers(String email, int page, int size) {
        // Return empty list when user service is unavailable
        return Collections.emptyList();
    }

    @Override
    public UserDTO getUserByUuid(String uuid) {
        // Return null when user service is unavailable
        return null;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        // Return null when user service is unavailable
        return null;
    }

    @Override
    public List<UserDTO> getUsersByUuids(List<String> uuids) {
        return Collections.emptyList();
    }
} 