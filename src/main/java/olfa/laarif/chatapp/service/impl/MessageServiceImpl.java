package olfa.laarif.chatapp.service.impl;

import olfa.laarif.chatapp.dto.*;
import olfa.laarif.chatapp.dto.notification.MessageDeletedNotification;
import olfa.laarif.chatapp.dto.notification.NewMessageNotification;
import olfa.laarif.chatapp.entity.*;
import olfa.laarif.chatapp.entity.listener.MessageActionEvent;
import olfa.laarif.chatapp.enums.*;
import olfa.laarif.chatapp.exception.*;
import olfa.laarif.chatapp.repository.*;
import olfa.laarif.chatapp.service.FileStorageService;
import olfa.laarif.chatapp.service.MessageService;
import olfa.laarif.chatapp.service.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.time.Instant.now;

@Service
public class MessageServiceImpl implements MessageService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;
    private final MessageEditHistoryRepository messageEditHistoryRepository;
    private final FileStorageService fileStorageService;
    private final SseService sseService;


    private final ApplicationEventPublisher eventPublisher;

    public MessageServiceImpl(UserRepository userRepository,
                              FriendshipRepository friendshipRepository,
                              ConversationRepository conversationRepository,
                              MessageRepository messageRepository,
                              AttachmentRepository attachmentRepository,
                              MessageEditHistoryRepository messageEditHistoryRepository,
                              FileStorageService fileStorageService,
                              SseService sseService,
                              ApplicationEventPublisher eventPublisher) { // Le paramètre sseService est maintenant bien placé ici
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.messageEditHistoryRepository = messageEditHistoryRepository;
        this.fileStorageService = fileStorageService;
        this.sseService = sseService;
        this.eventPublisher=eventPublisher;
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(String senderPhoneNumber, String receiverPhoneNumber, String content, MultipartFile file) {

        // 1. Validations de base (Utilisateurs & Amitié)
        UserEntity sender = userRepository.findByPhoneNumber(senderPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found: " + senderPhoneNumber));

        UserEntity receiver = userRepository.findByPhoneNumber(receiverPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException("No user found with phone number: " + receiverPhoneNumber));

        boolean areFriends = friendshipRepository.existsAcceptedFriendshipBetween(
                sender, receiver, FriendshipStatus.ACCEPTED);

        if (!areFriends) {
            throw new FriendshipNotFoundException("You can only send messages to your friends");
        }

        // 2. Récupération ou création de la conversation
        Instant now = Instant.now();
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

        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);

        // 3. Création et sauvegarde du Message
        MessageEntity message = MessageEntity.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build();

        MessageEntity savedMessage = messageRepository.save(message);

        // 4. Traitement de la pièce jointe si elle existe
        AttachmentEntity savedAttachment = null;
        if (file != null && !file.isEmpty()) {

            // Validation de la taille : 20 Mo max
            long maxBytes = 20971520;
            if (file.getSize() > maxBytes) {
                throw new IllegalArgumentException("File size exceeds maximum limit of 20 MB");
            }

            // Détermination du type d'attachment
            String mimeType = file.getContentType();
            AttachmentType attachmentType;
            if (mimeType != null && mimeType.startsWith("image/")) {
                attachmentType = AttachmentType.IMAGE;
            } else if (mimeType != null && mimeType.equals("application/pdf")) {
                attachmentType = AttachmentType.PDF;
            } else {
                throw new IllegalArgumentException("Only Images and PDFs are allowed");
            }

            // Sauvegarde physique du fichier
            String fileUrl = fileStorageService.store(file);

            // Sauvegarde en Base de Données
            AttachmentEntity attachment = AttachmentEntity.builder()
                    .message(savedMessage)
                    .filename(file.getOriginalFilename())
                    .url(fileUrl)
                    .mimeType(mimeType)
                    .type(attachmentType)
                    .sizeBytes((int) file.getSize())
                    .build();

            savedAttachment = attachmentRepository.save(attachment);
        }

        // 5. Notification en temps réel (SSE)
        sseService.notifyNewMessage(
                receiver.getId(),
                NewMessageNotification.builder()
                        .messageId(savedMessage.getId())
                        .conversationId(conversation.getId())
                        .senderUsername(sender.getUsername())
                        .senderPhoneNumber(sender.getPhoneNumber())
                        .contentPreview(savedMessage.getContent())
                        .sentAt(savedMessage.getCreatedAt())
                        .build()
        );

         // THEN - On vérifie qu'un log d'action SENT a été créé
        eventPublisher.publishEvent(new MessageActionEvent(message, MessageAction.SENT));

        // 6. Retour de la réponse complète
        return toResponse(savedMessage, savedAttachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(String userPhoneNumber, String conversationId) {
        UserEntity user = userRepository.findByPhoneNumber(userPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found: " + userPhoneNumber));

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));

        // 3. FIX: Check if the user is part of the conversation members list
        boolean isParticipant = conversation.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(user.getId()));

        if (!isParticipant) {
            throw new ConversationNotFoundException("Conversation not found: " + conversationId);
        }

        return messageRepository
                .findByConversationOrderByCreatedAtAsc(conversation)
                .stream()
                .map(msg -> {
                    AttachmentEntity attachment = attachmentRepository.findByMessage(msg)
                            .orElse(null);
                    return toResponse(msg, attachment);
                })
                .toList();
    }

    @Override
    @Transactional
    public MessageResponse editMessage(String userPhoneNumber, String messageId, String newContent) {
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (newContent.length() > 500) {
            throw new IllegalArgumentException("Message exceeds maximum limit of 500 characters");
        }

        UserEntity user = userRepository.findByPhoneNumber(userPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found: " + userPhoneNumber));

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found: " + messageId));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only edit your own messages");
        }

        if (message.getContent().equals(newContent)) {
            AttachmentEntity attachment = attachmentRepository.findByMessage(message).orElse(null);
            return toResponse(message, attachment);
        }

        MessageEditHistoryEntity history = MessageEditHistoryEntity.builder()
                .message(message)
                .originalContent(message.getContent())
                .newContent(newContent)
                .build();
        messageEditHistoryRepository.save(history);

        message.setContent(newContent);
        MessageEntity updatedMessage = messageRepository.save(message);

        AttachmentEntity attachment = attachmentRepository.findByMessage(updatedMessage).orElse(null);
        eventPublisher.publishEvent(new MessageActionEvent(message, MessageAction.EDITED));

        return toResponse(updatedMessage, attachment);
    }

    @Override
    @Transactional
    public void deleteMessage(String userPhoneNumber, String messageId) {
        UserEntity user = userRepository.findByPhoneNumber(userPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found: " + userPhoneNumber));

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found: " + messageId));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own messages");
        }

        message.setDeleted(true);
        message.setContent("Ce message a été supprimé.");
        messageRepository.save(message);

        attachmentRepository.findByMessage(message).ifPresent(attachment -> {
            fileStorageService.delete(attachment.getUrl());
            attachmentRepository.delete(attachment);
        });
        eventPublisher.publishEvent(new MessageActionEvent(message, MessageAction.DELETED));

        String recipientId = message.getConversation().getMembers().stream()
                .filter(m -> !m.getUser().getId().equals(user.getId()))
                .map(m -> m.getUser().getId())
                .findFirst()
                .orElseThrow();

        sseService.notifyMessageDeleted(
                recipientId,
                MessageDeletedNotification.builder()
                        .messageId(message.getId())
                        .conversationId(message.getConversation().getId())
                        .deletedAt(Instant.now())
                        .build()
        );

    }

    @Override
    @Transactional
    public void deleteAttachment(String userPhoneNumber, String messageId) {
        UserEntity user = userRepository.findByPhoneNumber(userPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found: " + userPhoneNumber));

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found: " + messageId));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete files from your own messages");
        }

        AttachmentEntity attachment = attachmentRepository.findByMessage(message)
                .orElseThrow(() -> new IllegalArgumentException("No attachment found for this message"));

        fileStorageService.delete(attachment.getUrl());
        attachmentRepository.delete(attachment);
        eventPublisher.publishEvent(new MessageActionEvent(message, MessageAction.DELETED));

    }

    private MessageResponse toResponse(MessageEntity entity, AttachmentEntity attachmentEntity) {
        AttachmentResponse attachmentResponse = null;
        if (attachmentEntity != null) {
            attachmentResponse = new AttachmentResponse(
                    attachmentEntity.getId(),
                    attachmentEntity.getFilename(),
                    attachmentEntity.getUrl(),
                    attachmentEntity.getMimeType(),
                    attachmentEntity.getType(),
                    attachmentEntity.getSizeBytes()
            );
        }

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
                attachmentResponse,
                entity.getCreatedAt()
        );
    }
}