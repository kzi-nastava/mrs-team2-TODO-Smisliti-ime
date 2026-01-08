package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name="activation_tokens")
@NoArgsConstructor @AllArgsConstructor
public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    private boolean isUsed;
    private LocalDateTime usedAt;
}