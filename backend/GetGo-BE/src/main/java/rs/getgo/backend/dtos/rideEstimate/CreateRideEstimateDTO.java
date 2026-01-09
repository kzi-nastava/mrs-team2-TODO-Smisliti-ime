package rs.getgo.backend.dtos.rideEstimate;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateRideEstimateDTO {
    private String origin;       // frontend šalje 'origin'
    private String destination;  // frontend šalje 'destination'

    private Double startLat;
    private Double startLon;
    private Double destLat;
    private Double destLon;

    private Double distanceMeters;
    private Integer estimatedTimeMinutes;
    private Double price;

    private LocalDateTime createdAt;

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    // ako frontend ipak šalje startAddress/destinationAddress, dodaj i te getters:
    public String getStartAddress() { return origin; }
    public String getDestinationAddress() { return destination; }
}