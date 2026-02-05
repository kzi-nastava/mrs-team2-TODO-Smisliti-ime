package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.MessageType;
import rs.getgo.backend.model.enums.SenderType;

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
    private MessageType type; // TEXT, IMAGE, SYSTEM

    @ManyToOne
    private Chat chat;

    @Enumerated(EnumType.STRING)
    private SenderType senderType; // USER / ADMIN
}