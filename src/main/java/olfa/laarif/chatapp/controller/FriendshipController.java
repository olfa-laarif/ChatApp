package olfa.laarif.chatapp.controller;

import olfa.laarif.chatapp.dto.FriendshipRequest;
import olfa.laarif.chatapp.dto.FriendshipResponse;
import olfa.laarif.chatapp.service.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/received")
    public ResponseEntity<List<FriendshipResponse>> listReceivedPendingRequests(
            Authentication authentication) {
        String receiverPhoneNumber = authentication.getName();
        return ResponseEntity.ok(friendshipService.listReceivedPendingRequests(receiverPhoneNumber));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<FriendshipResponse> acceptFriendRequest(
            Authentication authentication,
            @PathVariable("id") String friendshipId) {
        String receiverPhoneNumber = authentication.getName();
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(receiverPhoneNumber, friendshipId));
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<FriendshipResponse> declineFriendRequest(
            Authentication authentication,
            @PathVariable("id") String friendshipId) {
        String receiverPhoneNumber = authentication.getName();
        return ResponseEntity.ok(friendshipService.declineFriendRequest(receiverPhoneNumber, friendshipId));
    }
}