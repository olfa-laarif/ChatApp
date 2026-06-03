package olfa.laarif.chatapp.dto;

import java.time.Instant;

public record MessageResponse(
        String id,
        String conversationId,
        UserResponse sender,
        String content,
        boolean isDeleted,
        Instant createdAt
) {}