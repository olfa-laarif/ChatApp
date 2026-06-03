package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.MessageEditHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageEditHistoryRepository extends JpaRepository<MessageEditHistoryEntity, String> {
}