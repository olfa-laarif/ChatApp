package olfa.laarif.chatapp.controller;

import jakarta.validation.Valid;
import olfa.laarif.chatapp.dto.ConversationResponse;
import olfa.laarif.chatapp.dto.CreateGroupRequest;
import olfa.laarif.chatapp.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public ResponseEntity<ConversationResponse> createGroup(
            Authentication authentication,
            @Valid @RequestBody CreateGroupRequest request) {
        String creatorPhoneNumber = authentication.getName();
        ConversationResponse group = conversationService.createGroup(creatorPhoneNumber, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }
}