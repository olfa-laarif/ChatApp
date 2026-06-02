package olfa.laarif.chatapp.controller;

import olfa.laarif.chatapp.dto.FriendshipRequest;
import olfa.laarif.chatapp.dto.FriendshipResponse;
import olfa.laarif.chatapp.service.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping
    public ResponseEntity<FriendshipResponse> sendFriendRequest(
            Authentication authentication,
            @RequestBody FriendshipRequest request) {
        String requesterPhoneNumber = authentication.getName();
        FriendshipResponse response = friendshipService.sendFriendRequest(requesterPhoneNumber, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}