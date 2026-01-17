package rs.getgo.backend.dtos.userProfile;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserProfileDTO {
    private String name;
    private String surname;
    private String profilePictureUrl;
}
