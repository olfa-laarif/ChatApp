package olfa.laarif.chatapp.controller;

import olfa.laarif.chatapp.dto.EditMessageRequest;
import olfa.laarif.chatapp.dto.MessageResponse;
import olfa.laarif.chatapp.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> sendMessage(
            Authentication authentication,
            @RequestParam("conversationId") String conversationId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        String senderPhoneNumber = authentication.getName();

        if (conversationId == null || conversationId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if ((content == null || content.isBlank()) && (file == null || file.isEmpty())) {
            return ResponseEntity.badRequest().build();
        }

        MessageResponse response = messageService.sendMessage(senderPhoneNumber, conversationId, content, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(
            Authentication authentication,
            @PathVariable String conversationId) {

        String userPhoneNumber = authentication.getName();
        List<MessageResponse> responses = messageService.getConversationMessages(userPhoneNumber, conversationId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(
            Authentication authentication,
            @PathVariable String messageId,
            @RequestBody EditMessageRequest request) {

        String userPhoneNumber = authentication.getName();
        MessageResponse response = messageService.editMessage(userPhoneNumber, messageId, request.getContent());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<String> deleteMessage(
            Authentication authentication,
            @PathVariable String messageId) {

        String userPhoneNumber = authentication.getName();
        messageService.deleteMessage(userPhoneNumber, messageId);
        return ResponseEntity.ok("message deleted successfully");
    }

    @DeleteMapping("/{messageId}/attachment")
    public ResponseEntity<String> deleteAttachment(
            Authentication authentication,
            @PathVariable String messageId) {

        String userPhoneNumber = authentication.getName();
        messageService.deleteAttachment(userPhoneNumber, messageId);
        return ResponseEntity.ok("message deleted successfully");

    }
}