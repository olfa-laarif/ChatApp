package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.ConversationResponse;
import olfa.laarif.chatapp.dto.CreateGroupRequest;

import java.util.List;

public interface ConversationService {
    List<ConversationResponse> getUserConversationsOrderedByLastMessage(String phoneNumber);

    ConversationResponse createGroup(String creatorPhoneNumber, CreateGroupRequest request);
}