package olfa.laarif.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import olfa.laarif.chatapp.enums.ConversationType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;

@Entity
@Table(
        name = "conversations"
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ConversationEntity {

    @Id
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    // Relationship to link conversation with its participants
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationMemberEntity> members;

    // Relationship to link conversation with its messages
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageEntity> messages;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversationType", columnDefinition = "ENUM('DIRECT', 'GROUP')", nullable = false)
    private ConversationType conversationType = ConversationType.DIRECT;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;
}