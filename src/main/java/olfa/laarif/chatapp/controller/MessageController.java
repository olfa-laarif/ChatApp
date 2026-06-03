package olfa.laarif.chatapp.controller;

import olfa.laarif.chatapp.entity.MessageEntity;
import olfa.laarif.chatapp.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageEntity>> getConversationMessages(
            @RequestParam("user1") String user1Id,
            @RequestParam("user2") String user2Id) {

        List<MessageEntity> messages = messageService.getMessagesBetweenUsers(user1Id, user2Id);
        return ResponseEntity.ok(messages);
    }
}