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
public class FriendRequestAcceptedNotification {

    private String requestId;
    private String accepterId;
    private String accepterUsername;
    private Instant acceptedAt;
}