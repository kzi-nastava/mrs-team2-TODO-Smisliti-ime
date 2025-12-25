package rs.getgo.backend.model;

import lombok.Getter;
import lombok.Setter;
import rs.getgo.backend.enums.MessageType;

import java.time.LocalDateTime;

@Getter
@Setter
public class Notification {
    private Long id;
    private MessageType type;
    private Boolean isRead;
    private LocalDateTime timeStamp; //?
    private Message message;
}
