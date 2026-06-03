package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.notification.FriendRequestNotification;

public interface SseService {

    void notifyFriendRequestReceived(String receiverId, FriendRequestNotification payload);
}
