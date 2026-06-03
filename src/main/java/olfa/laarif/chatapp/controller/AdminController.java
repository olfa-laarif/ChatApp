package olfa.laarif.chatapp.controller;

import lombok.RequiredArgsConstructor;
import olfa.laarif.chatapp.dto.FileLogResponse;
import olfa.laarif.chatapp.dto.MessageLogResponse;
import olfa.laarif.chatapp.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/logs")
@RequiredArgsConstructor
public class AdminController {

    private final LogService adminLogService;

    @GetMapping("/messages")
    public ResponseEntity<List<MessageLogResponse>> getMessageLogs() {
        return ResponseEntity.ok(adminLogService.getAllMessageLogs());
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileLogResponse>> getFileLogs() {
        return ResponseEntity.ok(adminLogService.getAllFileLogs());
    }
}