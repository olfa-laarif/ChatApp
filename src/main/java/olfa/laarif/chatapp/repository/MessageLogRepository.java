package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.MessageLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLogEntity, String> {
}