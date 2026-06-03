package olfa.laarif.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import olfa.laarif.chatapp.enums.ConversationType;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ConversationResponse {
    private String conversationId;
    private ConversationType type;
    private Instant lastMessageAt;

    // DIRECT: id and username of the other member.
    // GROUP:  null for both — use `name` and `members` instead.
    private String friendId;
    private String friendUsername;

    // GROUP only: name of the group. null for DIRECT conversations.
    private String name;

    // Always populated. Useful for groups, also lets the front know the
    // participants of a DIRECT conversation.
    private List<UserResponse> members;

    // Content of the most recent message in the conversation. null if none yet.
    private String lastMessage;
}