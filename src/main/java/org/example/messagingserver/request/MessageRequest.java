package org.example.messagingserver.request;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class MessageRequest {
    private String to;
    private String text;
}
