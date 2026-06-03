package olfa.laarif.chatapp.controller;

import olfa.laarif.chatapp.dto.ConversationResponse;
import olfa.laarif.chatapp.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getUserConversations(
            Authentication authentication) {
        String requesterPhoneNumber = authentication.getName();
        List<ConversationResponse> conversations = conversationService.getUserConversationsOrderedByLastMessage(requesterPhoneNumber);
        return ResponseEntity.ok(conversations);
    }
}