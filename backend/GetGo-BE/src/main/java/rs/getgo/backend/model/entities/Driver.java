package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter @Getter
@Table(name="drivers")
@NoArgsConstructor @AllArgsConstructor
public class Driver extends User {

    private boolean isActive; // Is driver currently active on the app
//    private boolean isActivated; // Is driver's account activated
    private String profilePictureUrl;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(nullable = false)
    private boolean isActivated = false;

    public Boolean getActive() {
        return isActive;
    }
}