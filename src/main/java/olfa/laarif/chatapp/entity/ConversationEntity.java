package olfa.laarif.chatapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Data
public class ConversationEntity {
    @Id
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "user1_id", columnDefinition = "CHAR(36)", nullable = false)
    private String user1Id;

    @Column(name = "user2_id", columnDefinition = "CHAR(36)", nullable = false)
    private String user2Id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at", nullable = false)
    private LocalDateTime lastMessageAt;


}