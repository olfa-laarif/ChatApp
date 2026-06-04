package olfa.laarif.chatapp.entity.listener;

import lombok.RequiredArgsConstructor;
import olfa.laarif.chatapp.entity.AttachmentEntity;
import olfa.laarif.chatapp.entity.MessageLogEntity;
import olfa.laarif.chatapp.entity.listener.event.AttachementActionEvent;
import olfa.laarif.chatapp.entity.listener.event.MessageActionEvent;
import olfa.laarif.chatapp.enums.FileAction;
import olfa.laarif.chatapp.enums.MessageAction;
import olfa.laarif.chatapp.repository.AttachmentRepository;
import olfa.laarif.chatapp.repository.MessageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.time.Instant.now;

@Component
@RequiredArgsConstructor
public class MessageEventListener {
    @Autowired
    private MessageLogRepository messageLogRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener // <-- Dit à Spring d'exécuter cette méthode dès que l'événement est publié
    @Transactional // Ouvre une nouvelle transaction propre pour le log
    public void handleMessageAction(MessageActionEvent event) {
        MessageLogEntity log = MessageLogEntity.builder()
                .message(event.getMessage())
                .user(event.getMessage().getSender())
                .action(event.getAction())
                .createdAt(now())
                .build();

        messageLogRepository.save(log);


        // 2. CAS DELETE MESSAGE
        if (event.getAction() == MessageAction.DELETED) {
            Optional<AttachmentEntity> attachment = attachmentRepository.findByMessage(event.getMessage());

            attachment.ifPresent(
                    attachmentEntity -> eventPublisher.publishEvent(
                            new AttachementActionEvent(attachmentEntity, FileAction.DELETED)
                    ));
        }

    }
}