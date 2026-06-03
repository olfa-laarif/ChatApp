package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.FriendshipEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, String> {

    boolean existsByRequesterAndReceiverAndStatus(UserEntity requester,
                                                  UserEntity receiver,
                                                  FriendshipStatus status);

    List<FriendshipEntity> findByReceiverAndStatusOrderByCreatedAtDesc(UserEntity receiver,
                                                                       FriendshipStatus status);

    @Query("""
        SELECT COUNT(f) > 0 FROM FriendshipEntity f
        WHERE f.status = :status
          AND ((f.requester = :userA AND f.receiver = :userB)
            OR (f.requester = :userB AND f.receiver = :userA))
    """)
    boolean existsAcceptedFriendshipBetween(
            @Param("userA") UserEntity userA,
            @Param("userB") UserEntity userB,
            @Param("status") FriendshipStatus status
    );
}