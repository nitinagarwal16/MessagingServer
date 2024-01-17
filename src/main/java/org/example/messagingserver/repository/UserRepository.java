package org.example.messagingserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.messagingserver.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPasscode(String username, String passcode);
}
