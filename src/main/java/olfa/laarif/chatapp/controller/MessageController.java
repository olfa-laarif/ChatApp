package olfa.laarif.chatapp.controller;

import jakarta.validation.Valid;
import olfa.laarif.chatapp.dto.MessageResponse;
import olfa.laarif.chatapp.dto.SendMessageRequest;
import olfa.laarif.chatapp.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            Authentication authentication,
            @RequestParam("receiverPhoneNumber") String receiverPhoneNumber,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        String senderPhoneNumber = authentication.getName();

        if (receiverPhoneNumber == null || receiverPhoneNumber.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if ((content == null || content.isBlank()) && (file == null || file.isEmpty())) {
            return ResponseEntity.badRequest().build();
        }

        MessageResponse response = messageService.sendMessage(senderPhoneNumber, receiverPhoneNumber, content, file);
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
    public ResponseEntity<Void> deleteMessage(
            Authentication authentication,
            @PathVariable String messageId) {

        String userPhoneNumber = authentication.getName();
        return ResponseEntity.ok(
                messageService.getConversationMessages(userPhoneNumber, conversationId)
        );
    }

    @DeleteMapping("/{messageId}/attachment")
    public ResponseEntity<Void> deleteAttachment(
            Authentication authentication,
            @PathVariable String messageId) {

        String userPhoneNumber = authentication.getName();
        messageService.deleteAttachment(userPhoneNumber, messageId);
        return ResponseEntity.noContent().build();
    }
}