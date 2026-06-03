package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.MessageResponse;
import olfa.laarif.chatapp.dto.SendMessageRequest;

import java.util.List;

public interface MessageService {

    MessageResponse sendMessage(String senderPhoneNumber, SendMessageRequest request);

    List<MessageResponse> getConversationMessages(String userPhoneNumber, String conversationId);
}