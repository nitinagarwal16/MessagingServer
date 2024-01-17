package org.example.messagingserver.controller;

import org.example.messagingserver.entity.User;
import org.example.messagingserver.repository.UserRepository;
import org.example.messagingserver.request.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class MessagingController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/user")
    public ResponseEntity<String> createUser(@RequestBody UserRequest userRequest) {
        Optional<User> existingUser = userRepository.findByUsername(userRequest.getUsername());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"status\":\"failure\", \"message\":\"User already exists\"}");
        } else {
            User user = new User(userRequest.getUsername(), userRequest.getPasscode());
            userRepository.save(user);
            return ResponseEntity.ok("{\"status\":\"success\", \"message\":\"User created successfully\"}");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<String>> getUsers() {
        List<User> users = userRepository.findAll();
        List<String> usernames = new ArrayList<>();
        for (User user : users) {
            usernames.add(user.getUsername());
        }
        return ResponseEntity.ok(usernames);
    }
}
