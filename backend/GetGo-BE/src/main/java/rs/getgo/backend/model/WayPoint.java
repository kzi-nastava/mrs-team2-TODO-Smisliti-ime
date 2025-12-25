package rs.getgo.backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WayPoint {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String address;
}
