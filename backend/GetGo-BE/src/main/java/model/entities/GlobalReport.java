package model.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@DiscriminatorValue("GLOBAL")
@Table(name="global_reports")
@Getter @Setter
@NoArgsConstructor
public class GlobalReport extends Report {
}