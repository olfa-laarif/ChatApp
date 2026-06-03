package olfa.laarif.chatapp.service.impl;

import olfa.laarif.chatapp.dto.MessageResponse;
import olfa.laarif.chatapp.dto.SendMessageRequest;
import olfa.laarif.chatapp.dto.UserResponse;
import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.MessageEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.FriendshipStatus;
import olfa.laarif.chatapp.exception.ConversationNotFoundException;
import olfa.laarif.chatapp.exception.FriendshipNotFoundException;
import olfa.laarif.chatapp.exception.UserNotFoundException;
import olfa.laarif.chatapp.dto.notification.NewMessageNotification;
import olfa.laarif.chatapp.repository.ConversationRepository;
import olfa.laarif.chatapp.repository.FriendshipRepository;
import olfa.laarif.chatapp.repository.MessageRepository;
import olfa.laarif.chatapp.repository.UserRepository;
import olfa.laarif.chatapp.service.MessageService;
import olfa.laarif.chatapp.service.SseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {


    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SseService sseService;

    public MessageServiceImpl(UserRepository userRepository,
                              FriendshipRepository friendshipRepository,
                              ConversationRepository conversationRepository,
                              MessageRepository messageRepository,
                              SseService sseService) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.sseService = sseService;
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(String senderPhoneNumber, SendMessageRequest request) {

        UserEntity sender = userRepository.findByPhoneNumber(senderPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + senderPhoneNumber));

        UserEntity receiver = userRepository.findByPhoneNumber(request.receiverPhoneNumber())
                .orElseThrow(() -> new UserNotFoundException(
                        "No user found with phone number: " + request.receiverPhoneNumber()));

        boolean areFriends = friendshipRepository.existsAcceptedFriendshipBetween(
                sender, receiver, FriendshipStatus.ACCEPTED);

        if (!areFriends) {
            throw new FriendshipNotFoundException(
                    "You can only send messages to your friends");
        }

        Instant now = Instant.now();

        ConversationEntity conversation = conversationRepository
                .findBetween(sender, receiver)
                .orElseGet(() -> conversationRepository.save(
                        ConversationEntity.builder()
                                .user1(sender)
                                .user2(receiver)
                                .lastMessageAt(now)
                                .build()
                ));

        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);

        MessageEntity message = MessageEntity.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.content())
                .build();

        MessageEntity saved = messageRepository.save(message);

        sseService.notifyNewMessage(
                receiver.getId(),
                NewMessageNotification.builder()
                        .messageId(saved.getId())
                        .conversationId(conversation.getId())
                        .senderUsername(sender.getUsername())
                        .senderPhoneNumber(sender.getPhoneNumber())
                        .contentPreview(saved.getContent())
                        .sentAt(saved.getCreatedAt())
                        .build()
        );

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(String userPhoneNumber, String conversationId) {

        UserEntity user = userRepository.findByPhoneNumber(userPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + userPhoneNumber));

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversation not found: " + conversationId));

        boolean isParticipant = conversation.getUser1().getId().equals(user.getId())
                || conversation.getUser2().getId().equals(user.getId());

        if (!isParticipant) {
            throw new ConversationNotFoundException(
                    "Conversation not found: " + conversationId);
        }

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