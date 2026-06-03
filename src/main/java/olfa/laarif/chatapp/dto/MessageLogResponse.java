package olfa.laarif.chatapp.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record MessageLogResponse(
    String logId,
    String messageId,
    String userId,
    String username,
    String action,
    String message,
    Instant createdAt
) {}