package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@Table(name="drivers")
@NoArgsConstructor @AllArgsConstructor
public class Driver extends User {

    private boolean isActive;

    @OneToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<ProfileChangeRequest> changeRequests;

    @OneToMany(mappedBy = "driver")
    private List<Ride> rides;
}