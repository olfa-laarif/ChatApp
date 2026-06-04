package olfa.laarif.chatapp.entity.listener;

import lombok.RequiredArgsConstructor;
import olfa.laarif.chatapp.entity.FileLogEntity;
import olfa.laarif.chatapp.repository.FileLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class FileEventListener {

    private final FileLogRepository fileLogRepository;

    @EventListener
    @Transactional
    public void handleFileAction(FileActionEvent event) {
        FileLogEntity log = FileLogEntity.builder()
                .attachment(event.getAttachment())
                .user(event.getUser())
                .action(event.getAction())
                .createdAt(Instant.now())
                .build();

        fileLogRepository.save(log);
    }
}