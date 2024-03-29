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
    private static final String TEXTS = "texts";

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
        Optional<ResponseEntity<String>> userPresentAndLoggedInOptional = checkUserPresentAndLoggedIn(fromUser);
        if(userPresentAndLoggedInOptional.isPresent()) {
            return userPresentAndLoggedInOptional.get();
        }
        final Optional<User> toUserOptional = userRepository.findByUsername(messageRequest.getTo());
        if (toUserOptional.isPresent()) {
            User toUser = toUserOptional.get();
            User sender = userRepository.findByUsername(fromUser).get();
            if(sender.getBlockedUsers().contains(toUser.getUsername())) { //Receiver is blocked
                final JSONObject responseObject = getResponseObject(FAILURE);
                responseObject.put(MESSAGE, "Receiver is blocked");
                return ResponseEntity.ok(responseObject.toString());
            }
            Message message = new Message(sender, toUser, messageRequest.getText());
            messageRepository.save(message);
            return ResponseEntity.ok(getResponseObject(SUCCESS).toString());
        } else {
            final JSONObject responseObject = getResponseObject(FAILURE);
            responseObject.put(MESSAGE, "Receiver not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseObject.toString());
        }
    }

    @GetMapping("/user/{username}/message")
    public ResponseEntity<String> getUnreadMessages(@PathVariable String username) {
        Optional<ResponseEntity<String>> userPresentAndLoggedInOptional = checkUserPresentAndLoggedIn(username);
        if(userPresentAndLoggedInOptional.isPresent()) {
            return userPresentAndLoggedInOptional.get();
        }
        final List<Message> unreadMessages = messageRepository.findByReceiverUsernameAndReadIsFalse(username);

        if (unreadMessages.isEmpty()) {
            final JSONObject responseObject = getResponseObject(SUCCESS);
            responseObject.put(MESSAGE, "No new messages");
            return ResponseEntity.ok(responseObject.toString());
        } else {
            return ResponseEntity.ok(getFormattedResponseForFetchingUnreadMessages(unreadMessages));
        }
    }

    @GetMapping("/user/{username}/message/history")
    public ResponseEntity<String> getChatHistory(@PathVariable final String username, @RequestParam final String friend,
                                                 @RequestParam(required = false, defaultValue = "false") final boolean markAsRead) {
        Optional<ResponseEntity<String>> userPresentAndLoggedInOptional = checkUserPresentAndLoggedIn(username);
        if(userPresentAndLoggedInOptional.isPresent()) {
            return userPresentAndLoggedInOptional.get();
        }
        final List<Message> chatHistory = messageRepository
                .findBySenderUsernameAndReceiverUsernameOrSenderUsernameAndReceiverUsernameOrderByCreatedAtAsc(
                        username, friend, friend, username);
        final JSONObject responseObject = getResponseObject(SUCCESS);
        if (chatHistory.isEmpty()) {
            responseObject.put(MESSAGE, "No chat history");
        } else {
            if (markAsRead) {
                markMessagesAsRead(chatHistory, username);
            }
            responseObject.put(TEXTS, formatChatHistoryResponse(chatHistory));
        }
        return ResponseEntity.ok(responseObject.toString());
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody final UserRequest userRequest) {
        Optional<User> userOptional = userRepository.findByUsernameAndPasscode(userRequest.getUsername(), userRequest.getPasscode());
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            user.setLoggedIn(true);
            userRepository.save(user);
            final JSONObject jsonResponse = getResponseObject(SUCCESS);
            return ResponseEntity.ok(jsonResponse.toString());
        } else {
            final JSONObject jsonResponse = getResponseObject(FAILURE);
            jsonResponse.put(MESSAGE, "Invalid username or passcode");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonResponse.toString());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestBody final UserRequest userRequest) {
        Optional<User> userOptional = userRepository.findByUsername(userRequest.getUsername());
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            if(isUserLoggedIn(user.getUsername())) {
                user.setLoggedIn(false);
                userRepository.save(user);
            }
            final JSONObject jsonResponse = getResponseObject(SUCCESS);
            return ResponseEntity.ok(jsonResponse.toString());
        } else {
            final JSONObject jsonResponse = getResponseObject(FAILURE);
            jsonResponse.put(MESSAGE, "User not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResponse.toString());
        }
    }

    @PostMapping("/user/{username}/block")
    public ResponseEntity<String> blockUser(@PathVariable final String username, @RequestBody final UserRequest userRequest) {
        Optional<ResponseEntity<String>> userPresentAndLoggedInOptional = checkUserPresentAndLoggedIn(username);
        if(userPresentAndLoggedInOptional.isPresent()) {
            return userPresentAndLoggedInOptional.get();
        }
        User user = userRepository.findByUsername(username).get();
        Optional<User> blockedUserOptional = userRepository.findByUsername(userRequest.getUsername());
        if(blockedUserOptional.isPresent()) {
            final User blockedUser = blockedUserOptional.get();
            user.getBlockedUsers().add(blockedUser.getUsername());//block user
            userRepository.save(user);
            blockedUser.getBlockedUsers().add(user.getUsername());
            userRepository.save(blockedUser);
            final JSONObject jsonResponse = getResponseObject(SUCCESS);
            return ResponseEntity.ok(jsonResponse.toString());
        } else {
            final JSONObject jsonResponse = getResponseObject(FAILURE);
            jsonResponse.put(MESSAGE, "User to be be blocked not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResponse.toString());
        }
    }

    private void markMessagesAsRead(final List<Message> messages, final String receiverUsername) {
        for (final Message message : messages) {
            if (!message.isRead() && message.getReceiver().getUsername().equals(receiverUsername)) {
                message.setRead(true);
                messageRepository.save(message);
            }
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

    private JSONArray formatChatHistoryResponse(final List<Message> chatHistory) {
        final JSONArray textsArray = new JSONArray();

        for (Message message : chatHistory) {
            final JSONObject messageObject = new JSONObject();
            messageObject.put(message.getSender().getUsername(), message.getText());
            textsArray.put(messageObject);
        }

        return textsArray;
    }

    private Optional<ResponseEntity<String>> checkUserPresentAndLoggedIn(final String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        final JSONObject responseObject = getResponseObject(FAILURE);
        if(!userOptional.isPresent()) {
            responseObject.put(MESSAGE, "User not found");
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseObject.toString()));
        } else if(!isUserLoggedIn(username)) {
            responseObject.put(MESSAGE, "User not logged in");
            return Optional.of(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseObject.toString()));
        }
        return Optional.empty();
    }

    private boolean isUserLoggedIn(final String username) {
        final Optional<User> user = userRepository.findByUsername(username);
        return user.get().isLoggedIn();
    }



    private JSONObject getResponseObject(final String status) {
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(STATUS, status);
        return jsonResponse;
    }
}
