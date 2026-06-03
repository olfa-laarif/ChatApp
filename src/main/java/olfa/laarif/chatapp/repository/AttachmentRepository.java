package olfa.laarif.chatapp.repository;

import olfa.laarif.chatapp.entity.AttachmentEntity;
import olfa.laarif.chatapp.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, UUID> {

    Optional<AttachmentEntity> findByMessage(MessageEntity message);
}