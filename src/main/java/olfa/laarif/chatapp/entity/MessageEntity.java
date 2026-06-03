package olfa.laarif.chatapp.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "messages")
public class MessageEntity {
    @Id
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "conversation_id", columnDefinition = "CHAR(36)", nullable = false)
    private String conversationId;

    @Column(name = "sender_id", columnDefinition = "CHAR(36)", nullable = false)
    private String senderId;

    @Column(length = 500)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Getters, Setters, Constructeurs
    public String getId() {
        return id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}