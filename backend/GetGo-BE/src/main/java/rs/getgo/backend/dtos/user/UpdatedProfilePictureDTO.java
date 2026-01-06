package rs.getgo.backend.dtos.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdatedProfilePictureDTO {
    private String pictureUrl;
    private String message;
}
