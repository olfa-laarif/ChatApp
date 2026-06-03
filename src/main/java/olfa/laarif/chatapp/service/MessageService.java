package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.entity.MessageEntity;

import java.util.List;

public interface MessageService {
    List<MessageEntity> getMessagesBetweenUsers(String user1Id, String user2Id);
}