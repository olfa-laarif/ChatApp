package olfa.laarif.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import olfa.laarif.chatapp.enums.AttachmentType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(
        name = "attachments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attachment_message",
                columnNames = "message_id"
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttachmentEntity {

    @Id
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false, unique = true)
    private MessageEntity message;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttachmentType type;

    @Column(name = "size_bytes", nullable = false)
    private Integer sizeBytes;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}