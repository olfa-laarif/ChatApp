package olfa.laarif.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import olfa.laarif.chatapp.enums.MessageAction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(
        name = "message_logs",
        indexes = @Index(name = "idx_message_logs_message_id", columnList = "message_id")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MessageLogEntity {

    @Id
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private MessageEntity message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MessageAction action;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}