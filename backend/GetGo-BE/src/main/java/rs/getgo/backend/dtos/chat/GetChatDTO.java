package rs.getgo.backend.dtos.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetChatDTO {
    private Long id;
    private GetUserChatDTO user;
}
