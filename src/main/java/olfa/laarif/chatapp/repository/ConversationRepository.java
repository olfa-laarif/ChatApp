package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {



    @Query("SELECT c FROM ConversationEntity c " +
            "JOIN c.members m1 " +
            "JOIN c.members m2 " +
            "WHERE c.conversationType = :type " +
            "AND m1.user = :sender " +
            "AND m2.user = :receiver")
    Optional<ConversationEntity> findDirectConversationBetweenUsers(
            @Param("sender") UserEntity sender,
            @Param("receiver") UserEntity receiver,
            @Param("type") ConversationType type
    );
    @Query("SELECT DISTINCT c FROM ConversationEntity c " +
            "JOIN FETCH c.members m " +
            "JOIN FETCH m.user " +
            "WHERE c.id IN (SELECT conv.id FROM ConversationEntity conv JOIN conv.members mem WHERE mem.user.id = :userId) " +
            "ORDER BY c.lastMessageAt DESC")
    List<ConversationEntity> findAllConversationsByUserIdOrderByLastMessageAt(@Param("userId") String userId);
}