package ro.faur.apollo.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.home.domain.HomeGuest;

@Repository
public interface HomeGuestRepository extends JpaRepository<HomeGuest, String> {
}
