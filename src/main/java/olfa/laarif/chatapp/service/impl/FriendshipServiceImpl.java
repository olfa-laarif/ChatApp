package olfa.laarif.chatapp.service.impl;

import olfa.laarif.chatapp.dto.FriendshipRequest;
import olfa.laarif.chatapp.dto.FriendshipResponse;
import olfa.laarif.chatapp.dto.UserResponse;
import olfa.laarif.chatapp.dto.notification.FriendRequestAcceptedNotification;
import olfa.laarif.chatapp.dto.notification.FriendRequestNotification;
import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.ConversationMemberEntity;
import olfa.laarif.chatapp.entity.FriendshipEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.ConversationType;
import olfa.laarif.chatapp.enums.FriendshipStatus;
import olfa.laarif.chatapp.exception.FriendshipAlreadyExistsException;
import olfa.laarif.chatapp.exception.FriendshipNotFoundException;
import olfa.laarif.chatapp.exception.UserNotFoundException;
import olfa.laarif.chatapp.repository.ConversationRepository;
import olfa.laarif.chatapp.repository.FriendshipRepository;
import olfa.laarif.chatapp.repository.UserRepository;
import olfa.laarif.chatapp.service.FriendshipService;
import olfa.laarif.chatapp.service.SseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ConversationRepository conversationRepository;
    private final SseService sseService;

    public FriendshipServiceImpl(UserRepository userRepository,
                                 FriendshipRepository friendshipRepository,
                                 ConversationRepository conversationRepository,
                                 SseService sseService) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.conversationRepository = conversationRepository;
        this.sseService = sseService;
    }

    @Override
    @Transactional
    public FriendshipResponse sendFriendRequest(String requesterPhoneNumber, FriendshipRequest request) {
        UserEntity requester = userRepository.findByPhoneNumber(requesterPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + requesterPhoneNumber));

        UserEntity receiver = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new UserNotFoundException(
                        "No user found with phone number: " + request.phoneNumber()));

        if (requester.getId().equals(receiver.getId())) {
            throw new FriendshipAlreadyExistsException("You cannot send a friend request to yourself");
        }

        if (friendshipRepository.existsByRequesterAndReceiverAndStatus(
                requester, receiver, FriendshipStatus.PENDING)) {
            throw new FriendshipAlreadyExistsException(
                    "A pending friend request already exists for this user");
        }

        FriendshipEntity friendship = FriendshipEntity.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        FriendshipEntity saved = friendshipRepository.saveAndFlush(friendship);

        sseService.notifyFriendRequestReceived(
                receiver.getId(),
                FriendRequestNotification.builder()
                        .requestId(saved.getId())
                        .requesterId(requester.getId())
                        .requesterUsername(requester.getUsername())
                        .requesterPhoneNumber(requester.getPhoneNumber())
                        .sentAt(saved.getCreatedAt())
                        .build()
        );

        return toResponse(saved);
    }

    @Override
    @Transactional
    public FriendshipResponse acceptFriendRequest(String receiverPhoneNumber, String friendshipId) {
        return respondToFriendRequest(receiverPhoneNumber, friendshipId, FriendshipStatus.ACCEPTED);
    }

    @Override
    @Transactional
    public FriendshipResponse declineFriendRequest(String receiverPhoneNumber, String friendshipId) {
        return respondToFriendRequest(receiverPhoneNumber, friendshipId, FriendshipStatus.DECLINED);
    }

    @Override
    @Transactional
    public FriendshipResponse cancelFriendRequest(String requesterPhoneNumber, String friendshipId) {
        UserEntity requester = userRepository.findByPhoneNumber(requesterPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + requesterPhoneNumber));

        FriendshipEntity friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException(
                        "Friend request not found: " + friendshipId));

        if (!friendship.getRequester().getId().equals(requester.getId())) {
            throw new FriendshipNotFoundException(
                    "Friend request not found: " + friendshipId);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new FriendshipAlreadyExistsException(
                    "This friend request was already processed");
        }

        friendship.setStatus(FriendshipStatus.CANCELLED);
        return toResponse(friendshipRepository.save(friendship));
    }

    private FriendshipResponse respondToFriendRequest(String receiverPhoneNumber,
                                                     String friendshipId,
                                                     FriendshipStatus newStatus) {
        UserEntity receiver = userRepository.findByPhoneNumber(receiverPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + receiverPhoneNumber));

        FriendshipEntity friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException(
                        "Friend request not found: " + friendshipId));

        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            throw new FriendshipNotFoundException(
                    "Friend request not found: " + friendshipId);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new FriendshipAlreadyExistsException(
                    "This friend request was already processed");
        }

        friendship.setStatus(newStatus);
        FriendshipEntity saved = friendshipRepository.saveAndFlush(friendship);

        if (newStatus == FriendshipStatus.ACCEPTED) {
            ensureDirectConversation(saved.getRequester(), receiver);

            sseService.notifyFriendRequestAccepted(
                    saved.getRequester().getId(),
                    FriendRequestAcceptedNotification.builder()
                            .requestId(saved.getId())
                            .accepterId(receiver.getId())
                            .accepterUsername(receiver.getUsername())
                            .acceptedAt(saved.getUpdatedAt())
                            .build()
            );
        }

        return toResponse(saved);
    }

    // Idempotent: reuses an existing DIRECT conversation between the two users if any,
    // otherwise creates one. Called on friendship acceptance so the first message
    // doesn't have to bootstrap the conversation.
    private void ensureDirectConversation(UserEntity userA, UserEntity userB) {
        if (conversationRepository
                .findDirectConversationBetweenUsers(userA, userB, ConversationType.DIRECT)
                .isPresent()) {
            return;
        }

        Instant now = Instant.now();
        ConversationEntity conv = ConversationEntity.builder()
                .conversationType(ConversationType.DIRECT)
                .createdAt(now)
                .lastMessageAt(now)
                .members(new ArrayList<>())
                .build();

        conv.getMembers().add(ConversationMemberEntity.builder()
                .conversation(conv).user(userA).joinedAt(now).build());
        conv.getMembers().add(ConversationMemberEntity.builder()
                .conversation(conv).user(userB).joinedAt(now).build());

        conversationRepository.save(conv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipResponse> listReceivedPendingRequests(String receiverPhoneNumber) {
        UserEntity receiver = userRepository.findByPhoneNumber(receiverPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + receiverPhoneNumber));

        return friendshipRepository
                .findByReceiverAndStatusOrderByCreatedAtDesc(receiver, FriendshipStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FriendshipResponse toResponse(FriendshipEntity entity) {
        return new FriendshipResponse(
                entity.getId(),
                toUserResponse(entity.getRequester()),
                toUserResponse(entity.getReceiver()),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    private UserResponse toUserResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getPhoneNumber());
    }
}