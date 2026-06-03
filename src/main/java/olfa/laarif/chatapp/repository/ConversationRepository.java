package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {

    // On cherche la conversation peu importe qui est user1 ou user2 dans la BDD
    @Query("SELECT c FROM ConversationEntity c WHERE " +
            "(c.user1Id = :u1 AND c.user2Id = :u2) OR " +
            "(c.user1Id = :u2 AND c.user2Id = :u1)")
    Optional<ConversationEntity> findConversationBetweenUsers(@Param("u1") String u1, @Param("u2") String u2);
}