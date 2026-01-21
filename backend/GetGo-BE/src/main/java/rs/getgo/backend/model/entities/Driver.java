package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter @Getter
@Table(name="drivers")
@NoArgsConstructor @AllArgsConstructor
public class Driver extends User {

    private boolean isActive; // Is driver currently active on the app
    private String profilePictureUrl;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(nullable = false)
    private boolean isActivated = false;

    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime lastLocationUpdate;

    public Boolean getActive() {
        return isActive;
    }
}