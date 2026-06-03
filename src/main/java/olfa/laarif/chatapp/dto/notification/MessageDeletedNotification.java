package olfa.laarif.chatapp.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDeletedNotification {

    private String messageId;
    private String conversationId;
    private Instant deletedAt;
}
