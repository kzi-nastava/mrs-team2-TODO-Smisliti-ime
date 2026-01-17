package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.UserRole;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    private String name;
    private String surname;
    private String address;
    private String phone;
    private UserRole role;

    private boolean isBlocked; // quick check for whether user is blocked, for details check block note

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Chat chat;
}