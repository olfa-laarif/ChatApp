package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.MessageResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MessageService {
    MessageResponse sendMessage(String senderPhoneNumber, String receiverPhoneNumber, String content, MultipartFile file);

    List<MessageResponse> getConversationMessages(String userPhoneNumber, String conversationId);
}