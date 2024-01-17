package org.example.messagingserver.controller;

import org.example.messagingserver.entity.Message;
import org.example.messagingserver.entity.User;
import org.example.messagingserver.repository.MessageRepository;
import org.example.messagingserver.repository.UserRepository;
import org.example.messagingserver.request.MessageRequest;
import org.example.messagingserver.request.UserRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
public class MessagingController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private static final String STATUS = "status";
    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
    private static final String MESSAGE = "message";

    @PostMapping("/user")
    public ResponseEntity<String> createUser(@RequestBody final UserRequest userRequest) {
        final Optional<User> existingUser = userRepository.findByUsername(userRequest.getUsername());
        if (existingUser.isPresent()) {
            final JSONObject responseObject = getResponseObject(FAILURE);
            responseObject.put(MESSAGE, "User already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseObject.toString());
        } else {
            final User user = new User(userRequest.getUsername(), userRequest.getPasscode());
            userRepository.save(user);
            final JSONObject responseObject = getResponseObject(SUCCESS);
            responseObject.put(MESSAGE, "User created successfully");
            return ResponseEntity.ok(responseObject.toString());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<String>> getUsers() {
        final List<User> users = userRepository.findAll();
        final List<String> usernames = new ArrayList<>();
        for (final User user : users) {
            usernames.add(user.getUsername());
        }
        return ResponseEntity.ok(usernames);
    }

    @PostMapping("/user/{fromUser}/message")
    public ResponseEntity<String> sendMessage(@PathVariable final String fromUser, @RequestBody final MessageRequest messageRequest) {
        final Optional<User> toUserOptional = userRepository.findByUsername(messageRequest.getTo());
        if (toUserOptional.isPresent()) {
            User toUser = toUserOptional.get();
            User sender = userRepository.findByUsername(fromUser).orElseThrow();
            Message message = new Message(sender, toUser, messageRequest.getText());
            messageRepository.save(message);
            return ResponseEntity.ok(getResponseObject(SUCCESS).toString());
        } else {
            final JSONObject responseObject = getResponseObject(FAILURE);
            responseObject.put(MESSAGE, "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseObject.toString());
        }
    }

    @GetMapping("/user/{username}/message")
    public ResponseEntity<String> getUnreadMessages(@PathVariable String username) {
        final List<Message> unreadMessages = messageRepository.findByReceiverUsernameAndReadIsFalse(username);

        if (unreadMessages.isEmpty()) {
            final JSONObject responseObject = getResponseObject(SUCCESS);
            responseObject.put(MESSAGE, "No new messages");
            return ResponseEntity.ok(responseObject.toString());
        } else {
            return ResponseEntity.ok(getFormattedResponseForFetchingUnreadMessages(unreadMessages));
        }
    }

    private String getFormattedResponseForFetchingUnreadMessages(final List<Message> unreadMessages) {
        final Map<String, List<String>> messagesBySender = new HashMap<>();
        for (final Message message : unreadMessages) {
            final String senderUsername = message.getSender().getUsername();
            final String text = message.getText();
            messagesBySender.computeIfAbsent(senderUsername, k -> new ArrayList<>()).add(text);
            message.setRead(true);
            messageRepository.save(message);
        }

        final JSONObject jsonResponse = getResponseObject(SUCCESS);
        jsonResponse.put("message", "You have message(s)");

        final JSONArray dataArray = new JSONArray();
        messagesBySender.forEach((sender, texts) -> {
            JSONObject senderObject = new JSONObject();
            senderObject.put("username", sender);
            senderObject.put("texts", texts);
            dataArray.put(senderObject);
        });

        jsonResponse.put("data", dataArray);

        return jsonResponse.toString();
    }

    private JSONObject getResponseObject(final String status) {
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(STATUS, status);
        return jsonResponse;
    }
}
