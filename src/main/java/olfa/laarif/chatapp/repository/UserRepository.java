package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    boolean existsByRole(Role role);
}