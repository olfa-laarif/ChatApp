package olfa.laarif.chatapp.service.impl;

import olfa.laarif.chatapp.dto.*;
import olfa.laarif.chatapp.dto.notification.FileDeletedNotification;
import olfa.laarif.chatapp.dto.notification.MessageDeletedNotification;
import olfa.laarif.chatapp.dto.notification.MessageEditedNotification;
import olfa.laarif.chatapp.dto.notification.NewMessageNotification;
import olfa.laarif.chatapp.entity.*;
import olfa.laarif.chatapp.entity.listener.MessageActionEvent;
import olfa.laarif.chatapp.enums.*;
import olfa.laarif.chatapp.exception.*;
import olfa.laarif.chatapp.repository.*;
import olfa.laarif.chatapp.service.FileStorageService;
import olfa.laarif.chatapp.service.MessageService;
import olfa.laarif.chatapp.service.SseService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;


@Service
public class MessageServiceImpl implements MessageService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;
    private final MessageEditHistoryRepository messageEditHistoryRepository;
    private final FileStorageService fileStorageService;
    private final SseService sseService;
    private final ApplicationEventPublisher eventPublisher;

    public MessageServiceImpl(UserRepository userRepository,
                              ConversationRepository conversationRepository,
                              MessageRepository messageRepository,
                              AttachmentRepository attachmentRepository,
                              MessageEditHistoryRepository messageEditHistoryRepository,
                              FileStorageService fileStorageService,
                              SseService sseService,
                              ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.messageEditHistoryRepository = messageEditHistoryRepository;
        this.fileStorageService = fileStorageService;
        this.sseService = sseService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(String senderPhoneNumber, String conversationId, String content, MultipartFile file) {

        if (content != null && content.length() > 500) {
            throw new IllegalArgumentException("Message exceeds maximum limit of 500 characters");
        }

        UserEntity sender = userRepository.findByPhoneNumber(senderPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found: " + senderPhoneNumber));

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));

        // Membership is the single gatekeeper: DIRECT conversations are auto-created on
        // friendship acceptance, GROUP conversations enforce friendship at creation time.
        boolean isMember = conversation.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(sender.getId()));
        if (!isMember) {
            throw new ConversationNotFoundException("Conversation not found: " + conversationId);
        }

        Instant now = Instant.now();
        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);

        MessageEntity message = MessageEntity.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build();

        MessageEntity savedMessage = messageRepository.save(message);

        AttachmentEntity savedAttachment = null;
        if (file != null && !file.isEmpty()) {

            long maxBytes = 20971520;
            if (file.getSize() > maxBytes) {
                throw new IllegalArgumentException("File size exceeds maximum limit of 20 MB");
            }

            String mimeType = file.getContentType();
            AttachmentType attachmentType;
            if (mimeType != null && mimeType.startsWith("image/")) {
                attachmentType = AttachmentType.IMAGE;
            } else if (mimeType != null && mimeType.equals("application/pdf")) {
                attachmentType = AttachmentType.PDF;
            } else {
                throw new IllegalArgumentException("Only Images and PDFs are allowed");
            }

            String fileUrl = fileStorageService.store(file);

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

        // Fan-out SSE to every other member of the conversation.
        NewMessageNotification notification = NewMessageNotification.builder()
                .messageId(savedMessage.getId())
                .conversationId(conversation.getId())
                .senderUsername(sender.getUsername())
                .senderPhoneNumber(sender.getPhoneNumber())
                .contentPreview(savedMessage.getContent())
                .sentAt(savedMessage.getCreatedAt())
                .build();
        conversation.getMembers().stream()
                .map(m -> m.getUser().getId())
                .filter(id -> !id.equals(sender.getId()))
                .forEach(recipientId -> sseService.notifyNewMessage(recipientId, notification));

        eventPublisher.publishEvent(new MessageActionEvent(message, MessageAction.SENT));

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

        MessageEditedNotification notification = MessageEditedNotification.builder()
                .messageId(updatedMessage.getId())
                .conversationId(updatedMessage.getConversation().getId())
                .newContent(updatedMessage.getContent())
                .editedAt(Instant.now())
                .build();
        updatedMessage.getConversation().getMembers().stream()
                .map(m -> m.getUser().getId())
                .filter(id -> !id.equals(user.getId()))
                .forEach(recipientId -> sseService.notifyMessageEdited(recipientId, notification));

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

        MessageDeletedNotification notification = MessageDeletedNotification.builder()
                .messageId(message.getId())
                .conversationId(message.getConversation().getId())
                .deletedAt(Instant.now())
                .build();
        message.getConversation().getMembers().stream()
                .map(m -> m.getUser().getId())
                .filter(id -> !id.equals(user.getId()))
                .forEach(recipientId -> sseService.notifyMessageDeleted(recipientId, notification));
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

        String attachmentId = attachment.getId();

        fileStorageService.delete(attachment.getUrl());
        attachmentRepository.delete(attachment);
        eventPublisher.publishEvent(new MessageActionEvent(message, MessageAction.DELETED));

        FileDeletedNotification notification = FileDeletedNotification.builder()
                .attachmentId(attachmentId)
                .messageId(message.getId())
                .conversationId(message.getConversation().getId())
                .deletedAt(Instant.now())
                .build();
        message.getConversation().getMembers().stream()
                .map(m -> m.getUser().getId())
                .filter(id -> !id.equals(user.getId()))
                .forEach(recipientId -> sseService.notifyFileDeleted(recipientId, notification));
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