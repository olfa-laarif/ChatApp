package olfa.laarif.chatapp.service.impl;


import olfa.laarif.chatapp.dto.ConversationResponse;
import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.ConversationMemberEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.ConversationType;
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
        String userId = user.getId();

        List<ConversationEntity> conversations = conversationRepository.findAllConversationsByUserIdOrderByLastMessageAt(userId);

        return conversations.stream().map(conv -> {
            String chatName="";
            String targetId = null; // Groups don't have a single "friend ID", so we keep it null or set to group ID

            if (conv.getConversationType() == ConversationType.GROUP) {
                // Case 1: It's a group chat, use the group title
               // chatName = conv.get();
                targetId = conv.getId(); // Or leave as null depending on your frontend requirement
            } else {
                // Case 2: It's a 1-to-1 Direct Message, find the OTHER user
                UserEntity friend = conv.getMembers().stream()
                        .map(ConversationMemberEntity::getUser)
                        .filter(u -> !u.getId().equals(userId))
                        .findFirst()
                        .orElse(user); // Fallback to self if something is wrong

                chatName = friend.getUsername();
                targetId = friend.getId();
            }

            return new ConversationResponse(
                    conv.getId(),
                    conv.getLastMessageAt(),
                    targetId,
                    chatName
            );
        }).collect(Collectors.toList());
    }
}
