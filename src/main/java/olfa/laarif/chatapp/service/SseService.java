package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.notification.FriendRequestAcceptedNotification;
import olfa.laarif.chatapp.dto.notification.FriendRequestNotification;

public interface SseService {

    void notifyFriendRequestReceived(String receiverId, FriendRequestNotification payload);

    void notifyFriendRequestAccepted(String requesterId, FriendRequestAcceptedNotification payload);
}
