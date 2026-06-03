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
public class NewMessageNotification {

    private String messageId;
    private String conversationId;
    private String senderUsername;
    private String senderPhoneNumber;
    private String contentPreview;
    private Instant sentAt;
}
