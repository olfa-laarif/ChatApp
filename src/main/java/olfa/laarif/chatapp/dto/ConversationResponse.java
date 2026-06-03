package olfa.laarif.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ConversationResponse {
    private String conversationId;
    private Instant lastMessageAt;
    private List<String> friendId;
    private String lastMessage;
}