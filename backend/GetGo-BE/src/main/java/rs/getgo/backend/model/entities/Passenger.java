package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name="passengers")
@NoArgsConstructor
public class Passenger extends User {
    private String profilePictureUrl;
}