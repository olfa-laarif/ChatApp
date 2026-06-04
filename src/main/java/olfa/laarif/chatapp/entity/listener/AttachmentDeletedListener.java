package olfa.laarif.chatapp.entity.listener;

import lombok.RequiredArgsConstructor;
import olfa.laarif.chatapp.entity.AttachmentEntity;
import olfa.laarif.chatapp.entity.FileLogEntity;
import olfa.laarif.chatapp.entity.listener.event.AttachementActionEvent;
import olfa.laarif.chatapp.entity.listener.event.MessageActionEvent;
import olfa.laarif.chatapp.enums.FileAction;
import olfa.laarif.chatapp.repository.AttachmentRepository;
import olfa.laarif.chatapp.repository.FileLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static java.time.Instant.now;

@Component
@RequiredArgsConstructor
public class AttachmentDeletedListener {
    @Autowired
    private final FileLogRepository fileLogRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;

    @EventListener
    @Transactional
    public void handle(AttachementActionEvent event) {

        if (event.getAttachmentEntity()!=null) {
            // soft delete
            AttachmentEntity deletedAttachement = event.getAttachmentEntity();
            deletedAttachement.setDeleted(true);
            attachmentRepository.save(deletedAttachement);
            FileLogEntity filelog = FileLogEntity.builder()
                    .attachment(deletedAttachement)
                    .user(event.getAttachmentEntity().getMessage().getSender())
                    .action(FileAction.DELETED)
                    .createdAt(now())
                    .build();
            // file log
            fileLogRepository.save(
                    filelog);
        }
    }
}