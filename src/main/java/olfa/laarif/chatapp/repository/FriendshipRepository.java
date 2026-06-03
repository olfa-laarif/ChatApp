package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.FriendshipEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, String> {

    boolean existsByRequesterAndReceiverAndStatus(UserEntity requester,
                                                  UserEntity receiver,
                                                  FriendshipStatus status);

    List<FriendshipEntity> findByReceiverAndStatusOrderByCreatedAtDesc(UserEntity receiver,
                                                                      FriendshipStatus status);
}