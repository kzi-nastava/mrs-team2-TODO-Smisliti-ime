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
public class GetPersonalDriverChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String driverEmail;
    private String driverName;

    // Current personal data
    private String currentName;
    private String currentSurname;
    private String currentPhone;
    private String currentAddress;

    // Requested personal changes
    private String requestedName;
    private String requestedSurname;
    private String requestedPhone;
    private String requestedAddress;

    private String status;
    private LocalDateTime createdAt;
}
