package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "report_type")
@Table(name="reports")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dateRange;
    private int totalRides;
    private double totalKm;
    private double totalMoney;
    private double average;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}