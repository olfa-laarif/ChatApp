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
public class FriendRequestNotification {

    private String requestId;
    private String requesterId;
    private String requesterUsername;
    private String requesterPhoneNumber;
    private Instant sentAt;
}
