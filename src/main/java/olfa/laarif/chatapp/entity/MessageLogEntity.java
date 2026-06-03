package olfa.laarif.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "message_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageLogEntity {

    @Id
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;
    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "user_id", nullable = false)
    private String userId;


    @Column(nullable = false)
    private String action;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}