package rs.getgo.backend.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetDriverAvatarChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String driverEmail;
    private String driverName;

    private String currentProfilePictureUrl;

    private String requestedProfilePictureUrl;

    private String status;
    private LocalDateTime createdAt;
}