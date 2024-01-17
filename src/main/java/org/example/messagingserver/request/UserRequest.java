package org.example.messagingserver.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UserRequest {
    private String username;
    private String passcode;
}
