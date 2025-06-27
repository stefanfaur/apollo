package ro.faur.apollo.home.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ro.faur.apollo.shared.dto.UserDTO;

import java.util.Collections;
import java.util.List;

@Component
public class UserServiceClientFallback implements FallbackFactory<UserServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public List<UserDTO> searchUsers(String email, int page, int size) {
                logger.error("UserServiceClient.searchUsers(email={}, page={}, size={}) failed - falling back to empty list", 
                           email, page, size, cause);
                return Collections.emptyList();
            }

            @Override
            public UserDTO getUserByUuid(String uuid) {
                logger.error("UserServiceClient.getUserByUuid({}) failed - falling back to null", uuid, cause);
                return null;
            }

            @Override
            public UserDTO getUserByEmail(String email) {
                logger.error("UserServiceClient.getUserByEmail({}) failed - falling back to null", email, cause);
                return null;
            }

            @Override
            public List<UserDTO> getUsersByUuids(List<String> uuids) {
                logger.error("UserServiceClient.getUsersByUuids({}) failed - falling back to empty list", uuids, cause);
                return Collections.emptyList();
            }
        };
    }
} 