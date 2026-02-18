package rs.getgo.backend.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BlockUserResponseDTO {
    private Long id;
    private String email;
    private Boolean blocked;
    private String blockReason;
    private LocalDateTime blockedAt;
}
