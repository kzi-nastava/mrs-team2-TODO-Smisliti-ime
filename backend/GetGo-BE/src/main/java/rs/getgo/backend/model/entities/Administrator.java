package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@Table(name="administrators")
@NoArgsConstructor
public class Administrator extends User {
}
