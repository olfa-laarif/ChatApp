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
            @Valid @RequestBody SendMessageRequest request) {
        String senderPhoneNumber = authentication.getName();
        MessageResponse response = messageService.sendMessage(senderPhoneNumber, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(
            Authentication authentication,
            @PathVariable String conversationId) {
        String userPhoneNumber = authentication.getName();
        return ResponseEntity.ok(
                messageService.getConversationMessages(userPhoneNumber, conversationId)
        );
    }
}