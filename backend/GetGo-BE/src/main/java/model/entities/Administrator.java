package model.entities;

import jakarta.persistence.*;
import lombok.*;
import model.User;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Administrator extends User {
}
