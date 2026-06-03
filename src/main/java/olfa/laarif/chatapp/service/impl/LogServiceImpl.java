package olfa.laarif.chatapp.service.impl;

import lombok.RequiredArgsConstructor;
import olfa.laarif.chatapp.dto.FileLogResponse;
import olfa.laarif.chatapp.dto.MessageLogResponse;
import olfa.laarif.chatapp.repository.FileLogRepository;
import olfa.laarif.chatapp.repository.MessageLogRepository;
import olfa.laarif.chatapp.service.LogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final MessageLogRepository messageLogRepository;
    private final FileLogRepository fileLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MessageLogResponse> getAllMessageLogs() {
        return messageLogRepository.findAllLogs().stream()
                .map(log -> new MessageLogResponse(
                        log.getId(),
                        log.getMessage().getId(),
                        log.getUser().getId(),
                        log.getUser().getUsername(),
                        log.getAction().name(),
                        log.getMessage().getContent() != null && log.getMessage().getContent().length() > 30 
                                ? log.getMessage().getContent().substring(0, 30) + "..." 
                                : log.getMessage().getContent(),
                        log.getCreatedAt()
                )).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileLogResponse> getAllFileLogs() {
        return fileLogRepository.findAllLogs().stream()
                .map(log -> new FileLogResponse(
                        log.getId(),
                        log.getAttachment().getId(),
                        log.getUser().getId(),
                        log.getUser().getUsername(),
                        log.getAction().name(),
                        log.getAttachment().getFilename(),
                        log.getAttachment().getUrl(),
                        log.getCreatedAt()
                )).collect(Collectors.toList());
    }
}