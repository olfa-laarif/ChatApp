package olfa.laarif.chatapp.entity.listener;

import lombok.Getter;
import olfa.laarif.chatapp.entity.AttachmentEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.FileAction;

@Getter
public class FileActionEvent {
    private final AttachmentEntity attachment;
    private final UserEntity user;
    private final FileAction action;

    public FileActionEvent(AttachmentEntity attachment, UserEntity user, FileAction action) {
        this.attachment = attachment;
        this.user = user;
        this.action = action;
    }
}