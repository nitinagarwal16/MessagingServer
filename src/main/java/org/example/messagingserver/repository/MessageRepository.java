package org.example.messagingserver.repository;

import org.example.messagingserver.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverUsernameAndReadIsFalse(String receiverUsername);
    List<Message> findBySenderUsernameAndReceiverUsernameOrSenderUsernameAndReceiverUsernameOrderByCreatedAtAsc(
            String senderUsername, String receiverUsername, String senderUsername2, String receiverUsername2);
}
