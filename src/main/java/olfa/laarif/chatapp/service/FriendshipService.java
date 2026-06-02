package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.FriendshipRequest;
import olfa.laarif.chatapp.dto.FriendshipResponse;

public interface FriendshipService {

    FriendshipResponse sendFriendRequest(String requesterPhoneNumber, FriendshipRequest request);
}