package olfa.laarif.chatapp.service.impl;

import olfa.laarif.chatapp.entity.MessageEntity;
import olfa.laarif.chatapp.repository.ConversationRepository;
import olfa.laarif.chatapp.repository.MessageRepository;
import olfa.laarif.chatapp.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public MessageServiceImpl(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public List<MessageEntity> getMessagesBetweenUsers(String user1Id, String user2Id) {
        // 1. Trouver la conversation entre les deux utilisateurs
        return conversationRepository.findConversationBetweenUsers(user1Id, user2Id)
                // 2. Si elle existe, on récupère les messages ordonnés
                .map(conversation -> messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()))
                // 3. Sinon, on renvoie une liste vide (pas encore de messages)
                .orElse(Collections.emptyList());
    }
}