package rs.getgo.backend.dtos.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateMessageDTO {
    @NotBlank(message = "Message text cannot be empty")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String text;
}
