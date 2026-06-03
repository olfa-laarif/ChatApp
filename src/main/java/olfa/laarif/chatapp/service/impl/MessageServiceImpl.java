package olfa.laarif.chatapp.service.impl;

import olfa.laarif.chatapp.dto.MessageResponse;
import olfa.laarif.chatapp.dto.SendMessageRequest;
import olfa.laarif.chatapp.dto.UserResponse;
import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.ConversationMemberEntity;
import olfa.laarif.chatapp.entity.MessageEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.ConversationType;
import olfa.laarif.chatapp.enums.FriendshipStatus;
import olfa.laarif.chatapp.exception.ConversationNotFoundException;
import olfa.laarif.chatapp.exception.FriendshipNotFoundException;
import olfa.laarif.chatapp.exception.UserNotFoundException;
import olfa.laarif.chatapp.repository.ConversationRepository;
import olfa.laarif.chatapp.repository.FriendshipRepository;
import olfa.laarif.chatapp.repository.MessageRepository;
import olfa.laarif.chatapp.repository.UserRepository;
import olfa.laarif.chatapp.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.time.Instant.now;
@Service
public class MessageServiceImpl implements MessageService {


    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public MessageServiceImpl(UserRepository userRepository,
                              FriendshipRepository friendshipRepository,
                              ConversationRepository conversationRepository,
                              MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public MessageResponse sendMessage(String senderPhoneNumber, SendMessageRequest request) {

        // 1. Fetch sender
        UserEntity sender = userRepository.findByPhoneNumber(senderPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + senderPhoneNumber));

        // 2. Fetch receiver
        UserEntity receiver = userRepository.findByPhoneNumber(request.receiverPhoneNumber())
                .orElseThrow(() -> new UserNotFoundException(
                        "No user found with phone number: " + request.receiverPhoneNumber()));

        // 3. Friendship check (Keep this rule for 1-to-1 DMs)
        boolean areFriends = friendshipRepository.existsAcceptedFriendshipBetween(
                sender, receiver, FriendshipStatus.ACCEPTED);

        if (!areFriends) {
            throw new FriendshipNotFoundException(
                    "You can only send messages to your friends");
        }


        // 4. Find or Create the DIRECT conversation with membership setup
        ConversationEntity conversation = conversationRepository
                .findDirectConversationBetweenUsers(sender, receiver,ConversationType.DIRECT)
                .orElseGet(() -> {
                    // Initialize the base conversation
                    ConversationEntity newConv = ConversationEntity.builder()
                            .conversationType(ConversationType.DIRECT)

                            .createdAt(now())
                            .lastMessageAt(now())
                            .members(new ArrayList<>())
                            .build();

                    // Create and attach both members
                    ConversationMemberEntity memberSender = ConversationMemberEntity.builder()
                            .conversation(newConv)
                            .user(sender)

                            .joinedAt(now())
                            .build();

                    // Note: Change ConversationMemberEntity to whatever your exact class name is
                    ConversationMemberEntity memberReceiver = ConversationMemberEntity.builder()
                            .conversation(newConv)
                            .user(receiver)
                          //  .role(MemberRole.MEMBER)
                            .joinedAt(now())
                            .build();

                    newConv.getMembers().add(memberSender);
                    newConv.getMembers().add(memberReceiver);

                    return conversationRepository.save(newConv);
                });

        // 5. Update the activity timestamp
        conversation.setLastMessageAt(now());
        conversationRepository.save(conversation);

        // 6. Build and save the actual message payload
        MessageEntity message = MessageEntity.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.content())
                .createdAt(now()) // Set if not fully automated by database
                .build();

        MessageEntity saved = messageRepository.save(message);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(String userPhoneNumber, String conversationId) {

        // 1. Fetch the authenticated user
        UserEntity user = userRepository.findByPhoneNumber(userPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + userPhoneNumber));

        // 2. Fetch the conversation
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversation not found: " + conversationId));

        // 3. FIX: Check if the user is part of the conversation members list
        boolean isParticipant = conversation.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(user.getId()));

        if (!isParticipant) {
            // Return a 404/Not Found for security reasons so outsiders don't know the chat exists
            throw new ConversationNotFoundException(
                    "Conversation not found: " + conversationId);
        }

        // 4. Fetch and map the messages
        return messageRepository
                .findByConversationOrderByCreatedAtAsc(conversation)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    private MessageResponse toResponse(MessageEntity entity) {
        return new MessageResponse(
                entity.getId(),
                entity.getConversation().getId(),
                new UserResponse(
                        entity.getSender().getId(),
                        entity.getSender().getUsername(),
                        entity.getSender().getPhoneNumber()
                ),
                entity.getContent(),
                entity.isDeleted(),
                entity.getCreatedAt()
        );
    }
}