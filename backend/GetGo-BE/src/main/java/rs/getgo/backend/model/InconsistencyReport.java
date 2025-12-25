package rs.getgo.backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InconsistencyReport {
    private Long id;
    private String description;
    private Long PassengerId;
    private Long rideId;
}
