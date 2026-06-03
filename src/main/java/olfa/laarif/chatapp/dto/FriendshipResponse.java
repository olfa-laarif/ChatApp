package olfa.laarif.chatapp.dto;

import olfa.laarif.chatapp.enums.FriendshipStatus;

import java.time.Instant;

public record FriendshipResponse(String id,
                                 UserResponse requester,
                                 UserResponse receiver,
                                 FriendshipStatus status,
                                 Instant createdAt) {
}