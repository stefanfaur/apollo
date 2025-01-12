package ro.faur.apollo.libs.auth.user.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.auth.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    @Cacheable(value = "users", key = "#uuid", unless = "#result == null")
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
