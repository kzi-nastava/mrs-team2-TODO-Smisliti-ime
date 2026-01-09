package rs.getgo.backend.model.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@DiscriminatorValue("GLOBAL")
@Getter @Setter
@NoArgsConstructor
public class GlobalReport extends Report {
}