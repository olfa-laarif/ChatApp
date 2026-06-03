package olfa.laarif.chatapp.entity.listener;

import lombok.RequiredArgsConstructor;
import olfa.laarif.chatapp.entity.MessageLogEntity;
import olfa.laarif.chatapp.repository.MessageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MessageEventListener {
    @Autowired
    private MessageLogRepository messageLogRepository;

    @EventListener // <-- Dit à Spring d'exécuter cette méthode dès que l'événement est publié
    @Transactional // Ouvre une nouvelle transaction propre pour le log
    public void handleMessageAction(MessageActionEvent event) {
        MessageLogEntity log = MessageLogEntity.builder()
                .messageId(event.getMessage().getId())
                .userId(event.getMessage().getSenderId())
                .action(event.getAction().name())
                .createdAt(LocalDateTime.now())
                .build();

        messageLogRepository.save(log);
    }
}