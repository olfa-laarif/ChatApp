package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.ConversationResponse;

import java.util.List;

public interface ConversationService{
    public List<ConversationResponse> getUserConversationsOrderedByLastMessage(String phoneNumber);

}