package olfa.laarif.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class ConversationResponse {
    private String conversationId;
    private Instant lastMessageAt;
    private String friendId;
    private String friendUsername;
}