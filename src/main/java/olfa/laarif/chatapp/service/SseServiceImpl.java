package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.notification.FriendRequestAcceptedNotification;
import olfa.laarif.chatapp.dto.notification.FriendRequestNotification;
import olfa.laarif.chatapp.dto.notification.MessageDeletedNotification;
import olfa.laarif.chatapp.dto.notification.NewMessageNotification;
import olfa.laarif.chatapp.dto.notification.SseEvent;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.repository.UserRepository;
import olfa.laarif.chatapp.sse.SseEmitterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class SseServiceImpl implements SseService {

    @Autowired
    private SseEmitterRegistry registry;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public void notifyFriendRequestReceived(String receiverId, FriendRequestNotification payload) {
        sendEvent(receiverId, "FRIEND_REQUEST_RECEIVED", payload,
                user -> emailService.sendFriendRequestEmail(user.getEmail(), user.getUsername(), payload));
    }

    @Override
    public void notifyFriendRequestAccepted(String requesterId, FriendRequestAcceptedNotification payload) {
        sendEvent(requesterId, "FRIEND_REQUEST_ACCEPTED", payload,
                user -> emailService.sendFriendRequestAcceptedEmail(user.getEmail(), user.getUsername(), payload));
    }

    @Override
    public void notifyNewMessage(String recipientId, NewMessageNotification payload) {
        sendEvent(recipientId, "NEW_MESSAGE", payload, null);
    }

    @Override
    public void notifyMessageDeleted(String recipientId, MessageDeletedNotification payload) {
        sendEvent(recipientId, "MESSAGE_DELETED", payload, null);
    }

    <T> void sendEvent(String userId, String eventType, T payload, Consumer<UserEntity> emailFallback) {
        Optional<SseEmitter> emitterOpt = registry.getEmitter(userId);
        if (emitterOpt.isPresent()) {
            try {
                emitterOpt.get().send(
                        SseEmitter.event()
                                .name(eventType)
                                .data(new SseEvent<>(eventType, payload), MediaType.APPLICATION_JSON)
                );
            } catch (IOException e) {
                registry.remove(userId);
                fallbackToEmail(userId, emailFallback);
            }
        } else {
            fallbackToEmail(userId, emailFallback);
        }
    }

    private void fallbackToEmail(String userId, Consumer<UserEntity> emailFallback) {
        if (emailFallback != null) {
            userRepository.findById(userId).ifPresent(emailFallback);
        }
    }
}
