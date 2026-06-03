package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {

    @Query("""
        SELECT c FROM ConversationEntity c
        WHERE (c.user1 = :userA AND c.user2 = :userB)
           OR (c.user1 = :userB AND c.user2 = :userA)
    """)
    Optional<ConversationEntity> findBetween(
            @Param("userA") UserEntity userA,
            @Param("userB") UserEntity userB
    );
}