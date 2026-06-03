package olfa.laarif.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "message_edit_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MessageEditHistoryEntity {

    @Id
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private MessageEntity message;

    @Column(name = "original_content", nullable = false, length = 500)
    private String originalContent;

    @Column(name = "new_content", nullable = false, length = 500)
    private String newContent;

    @CreationTimestamp
    @Column(name = "edited_at", nullable = false, updatable = false)
    private Instant editedAt;
}