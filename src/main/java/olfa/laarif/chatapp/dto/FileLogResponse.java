package olfa.laarif.chatapp.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record FileLogResponse(
    String logId,
    String attachmentId,
    String userId,
    String username,
    String action,
    String filename,
    String fileUrl,
    Instant createdAt
) {}