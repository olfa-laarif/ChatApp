package olfa.laarif.chatapp.service.impl;

import olfa.laarif.chatapp.dto.*;
import olfa.laarif.chatapp.entity.*;
import olfa.laarif.chatapp.enums.*;
import olfa.laarif.chatapp.exception.*;
import olfa.laarif.chatapp.repository.*;
import olfa.laarif.chatapp.service.FileStorageService;
import olfa.laarif.chatapp.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    public MessageServiceImpl(UserRepository userRepository,
                              FriendshipRepository friendshipRepository,
                              ConversationRepository conversationRepository,
                              MessageRepository messageRepository,
                              AttachmentRepository attachmentRepository,
                              FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
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

            // Validation de la taille : 20 Mo max (20 * 1024 * 1024 octets)
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

        return toResponse(savedMessage, savedAttachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(String userPhoneNumber, String conversationId) {
        UserEntity user = userRepository.findByPhoneNumber(userPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found: " + userPhoneNumber));

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));

        boolean isParticipant = conversation.getUser1().getId().equals(user.getId())
                || conversation.getUser2().getId().equals(user.getId());

        if (!isParticipant) {
            throw new ConversationNotFoundException("Conversation not found: " + conversationId);
        }

        return messageRepository
                .findByConversationOrderByCreatedAtAsc(conversation)
                .stream()
                .map(msg -> {
                    // Recherche de la pièce jointe associée à ce message
                    AttachmentEntity attachment = attachmentRepository.findByMessage(msg)
                            .orElse(null);
                    return toResponse(msg, attachment);
                })
                .toList();
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