package olfa.laarif.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import olfa.laarif.chatapp.enums.FileAction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(
        name = "file_logs",
        indexes = @Index(name = "idx_file_logs_attachment_id", columnList = "attachment_id")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FileLogEntity {

    @Id
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachment_id", nullable = false)
    private AttachmentEntity attachment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FileAction action;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}