package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.MessageLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLogEntity, String> {

    @Query("SELECT ml FROM MessageLogEntity ml JOIN FETCH ml.user JOIN FETCH ml.message ORDER BY ml.createdAt DESC")
    List<MessageLogEntity> findAllLogs();
}