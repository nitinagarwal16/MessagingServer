package org.example.messagingserver.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@EqualsAndHashCode
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Setter
    private boolean read;

    public Message() {
        this.createdAt = LocalDateTime.now();
    }

    public Message(final User sender, final User receiver, final String text) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }
}

