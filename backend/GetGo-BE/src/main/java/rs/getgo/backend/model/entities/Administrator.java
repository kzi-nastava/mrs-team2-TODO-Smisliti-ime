package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
public class Administrator extends User {
}
