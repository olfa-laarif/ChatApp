package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.FriendshipEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, String> {

    boolean existsByRequesterAndReceiverAndStatus(UserEntity requester,
                                                  UserEntity receiver,
                                                  FriendshipStatus status);
}