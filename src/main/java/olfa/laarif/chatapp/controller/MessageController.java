package olfa.laarif.chatapp.controller;

import jakarta.validation.Valid;
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
            @RequestParam("receiverPhoneNumber") String receiverPhoneNumber,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        String senderPhoneNumber = authentication.getName();

        // Validation manuelle minimale
        if (receiverPhoneNumber == null || receiverPhoneNumber.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Un message ne peut pas être totalement vide (il faut du texte ou un fichier)
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
        return ResponseEntity.ok(
                messageService.getConversationMessages(userPhoneNumber, conversationId)
        );
    }
}