package ro.faur.apollo.libs.auth.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.faur.apollo.libs.auth.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
}
