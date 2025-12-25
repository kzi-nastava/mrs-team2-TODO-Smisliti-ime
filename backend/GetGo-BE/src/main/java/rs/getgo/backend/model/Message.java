package rs.getgo.backend.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

public class Message {
    private Long id;
    private String text;
    private LocalDateTime timestamp;

}
