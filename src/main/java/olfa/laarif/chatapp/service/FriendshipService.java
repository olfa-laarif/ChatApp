package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.FriendshipRequest;
import olfa.laarif.chatapp.dto.FriendshipResponse;

import java.util.List;

public interface FriendshipService {

    FriendshipResponse sendFriendRequest(String requesterPhoneNumber, FriendshipRequest request);

    List<FriendshipResponse> listReceivedPendingRequests(String receiverPhoneNumber);
}