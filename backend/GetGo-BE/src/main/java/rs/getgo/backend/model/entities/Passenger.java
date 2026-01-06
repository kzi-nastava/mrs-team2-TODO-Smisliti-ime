package rs.getgo.backend.model.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@DiscriminatorValue("PASSENGER")
@NoArgsConstructor
public class Passenger extends User {
}