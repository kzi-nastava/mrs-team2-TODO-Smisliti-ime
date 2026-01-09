package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.MessageType;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name="messages")
@NoArgsConstructor @AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @ManyToOne
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
}