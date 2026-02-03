package rs.getgo.backend.dtos.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.SenderType;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetMessageDTO {
    private String text;
    private SenderType senderType;
    private LocalDateTime timestamp;
}
