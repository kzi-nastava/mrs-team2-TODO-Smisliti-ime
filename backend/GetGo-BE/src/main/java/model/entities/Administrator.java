package model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@DiscriminatorValue("ADMIN")
@Table(name="administrators")
@NoArgsConstructor
public class Administrator extends User {
}
