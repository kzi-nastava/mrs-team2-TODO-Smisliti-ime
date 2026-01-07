package model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@DiscriminatorValue("PASSENGER")
@Table(name="passengers")
@NoArgsConstructor
public class Passenger extends User {
}