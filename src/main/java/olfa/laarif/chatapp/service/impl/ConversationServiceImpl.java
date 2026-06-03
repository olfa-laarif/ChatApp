package olfa.laarif.chatapp.service.impl;


import olfa.laarif.chatapp.dto.ConversationResponse;
import olfa.laarif.chatapp.dto.CreateGroupRequest;
import olfa.laarif.chatapp.dto.UserResponse;
import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.ConversationMemberEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.ConversationType;
import olfa.laarif.chatapp.enums.FriendshipStatus;
import olfa.laarif.chatapp.exception.FriendshipNotFoundException;
import olfa.laarif.chatapp.exception.UserNotFoundException;
import olfa.laarif.chatapp.repository.ConversationRepository;
import olfa.laarif.chatapp.repository.FriendshipRepository;
import olfa.laarif.chatapp.repository.UserRepository;
import olfa.laarif.chatapp.service.ConversationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   UserRepository userRepository,
                                   FriendshipRepository friendshipRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }


    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversationsOrderedByLastMessage(String phoneNumber) {
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + phoneNumber));
        String userId = user.getId();

        List<ConversationEntity> conversations = conversationRepository.findAllConversationsByUserIdOrderByLastMessageAt(userId);

        return conversations.stream().map(conv -> toResponse(conv, userId)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConversationResponse createGroup(String creatorPhoneNumber, CreateGroupRequest request) {
        UserEntity creator = userRepository.findByPhoneNumber(creatorPhoneNumber)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + creatorPhoneNumber));

        // Resolve all requested members, ignoring the creator if listed.
        // LinkedHashSet keeps insertion order and dedupes the input list.
        Set<String> uniquePhones = new LinkedHashSet<>(request.memberPhoneNumbers());
        uniquePhones.remove(creator.getPhoneNumber());

        List<UserEntity> otherMembers = new ArrayList<>();
        for (String phone : uniquePhones) {
            UserEntity member = userRepository.findByPhoneNumber(phone)
                    .orElseThrow(() -> new UserNotFoundException(
                            "No user found with phone number: " + phone));

            // The creator can only add people they are friends with.
            boolean areFriends = friendshipRepository.existsAcceptedFriendshipBetween(
                    creator, member, FriendshipStatus.ACCEPTED);
            if (!areFriends) {
                throw new FriendshipNotFoundException(
                        "You can only add friends to a group: " + phone);
            }

            otherMembers.add(member);
        }

        if (otherMembers.isEmpty()) {
            throw new IllegalArgumentException("A group must have at least one other member than the creator");
        }

        Instant now = Instant.now();
        ConversationEntity group = ConversationEntity.builder()
                .conversationType(ConversationType.GROUP)
                .name(request.name())
                .createdAt(now)
                .lastMessageAt(now)
                .members(new ArrayList<>())
                .build();

        // Creator first, then the rest — order is preserved in the response.
        group.getMembers().add(buildMember(group, creator, now));
        for (UserEntity member : otherMembers) {
            group.getMembers().add(buildMember(group, member, now));
        }

        ConversationEntity saved = conversationRepository.save(group);
        return toResponse(saved, creator.getId());
    }

    private ConversationMemberEntity buildMember(ConversationEntity conversation, UserEntity user, Instant joinedAt) {
        return ConversationMemberEntity.builder()
                .conversation(conversation)
                .user(user)
                .joinedAt(joinedAt)
                .build();
    }

    // Maps an entity to a response, contextualized to the viewer (currentUserId)
    // so DIRECT conversations can expose "the other member" as the friend.
    private ConversationResponse toResponse(ConversationEntity conv, String currentUserId) {
        List<UserResponse> memberResponses = conv.getMembers().stream()
                .map(m -> new UserResponse(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        m.getUser().getPhoneNumber()))
                .collect(Collectors.toList());

        String friendId = null;
        String friendUsername = null;
        String groupName = null;

        if (conv.getConversationType() == ConversationType.GROUP) {
            groupName = conv.getName();
        } else {
            // DIRECT: find the other member (not the current user).
            UserEntity other = conv.getMembers().stream()
                    .map(ConversationMemberEntity::getUser)
                    .filter(u -> !u.getId().equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (other != null) {
                friendId = other.getId();
                friendUsername = other.getUsername();
            }
        }

        return new ConversationResponse(
                conv.getId(),
                conv.getConversationType(),
                conv.getLastMessageAt(),
                friendId,
                friendUsername,
                groupName,
                memberResponses
        );
    }
}