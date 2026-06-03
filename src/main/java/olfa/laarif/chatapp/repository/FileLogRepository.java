package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.FileLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileLogRepository extends JpaRepository<FileLogEntity, String> {

    @Query("SELECT fl FROM FileLogEntity fl JOIN FETCH fl.user JOIN FETCH fl.attachment ORDER BY fl.createdAt DESC")
    List<FileLogEntity> findAllLogs();
}