package olfa.laarif.chatapp.service.impl;


import olfa.laarif.chatapp.dto.ConversationResponse;
import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.exception.UserNotFoundException;
import olfa.laarif.chatapp.repository.ConversationRepository;
import olfa.laarif.chatapp.repository.UserRepository;
import olfa.laarif.chatapp.service.ConversationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository,UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository=userRepository;
    }


    public List<ConversationResponse> getUserConversationsOrderedByLastMessage(String phoneNumber) {
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + phoneNumber));
        String userId= user.getId();
        List<ConversationEntity> conversations = conversationRepository.findAllConversationsByUserIdOrderByLastMessageBy(userId);

        return conversations.stream().map(conv -> {
            // On détermine dynamiquement qui est l'autre utilisateur (l'interlocuteur)
            UserEntity friend = conv.getUser1().getId().equals(userId) ? conv.getUser2() : conv.getUser1();

            return new ConversationResponse(
                    conv.getId(),
                    conv.getLastMessageAt(),
                    friend.getId(),
                    friend.getUsername()
            );
        }).collect(Collectors.toList());
    }
}
