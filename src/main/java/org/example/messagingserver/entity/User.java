package org.example.messagingserver.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@ToString
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passcode;

    @Setter
    @Column(nullable = false)
    private boolean isLoggedIn;

    @Column(nullable = false)
    private Set<String> blockedUsers;

    public User() {

    }

    public User(final String username, final String passcode) {
        this.username = username;
        this.passcode = passcode;
        this.isLoggedIn = false;
        this.blockedUsers = new HashSet<>();
    }
}
