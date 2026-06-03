package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.notification.FriendRequestAcceptedNotification;
import olfa.laarif.chatapp.dto.notification.FriendRequestNotification;
import olfa.laarif.chatapp.dto.notification.MessageDeletedNotification;
import olfa.laarif.chatapp.dto.notification.MessageEditedNotification;
import olfa.laarif.chatapp.dto.notification.NewMessageNotification;

public interface SseService {

    void notifyFriendRequestReceived(String receiverId, FriendRequestNotification payload);

    void notifyFriendRequestAccepted(String requesterId, FriendRequestAcceptedNotification payload);

    void notifyNewMessage(String recipientId, NewMessageNotification payload);

    void notifyMessageDeleted(String recipientId, MessageDeletedNotification payload);

    void notifyMessageEdited(String recipientId, MessageEditedNotification payload);
}
