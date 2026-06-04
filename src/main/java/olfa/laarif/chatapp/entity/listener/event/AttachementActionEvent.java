package olfa.laarif.chatapp.entity.listener.event;

import lombok.Getter;
import olfa.laarif.chatapp.entity.AttachmentEntity;
import olfa.laarif.chatapp.entity.MessageEntity;
import olfa.laarif.chatapp.enums.FileAction;
import olfa.laarif.chatapp.enums.MessageAction;

@Getter
public class AttachementActionEvent {
    private final AttachmentEntity attachmentEntity;
    private final FileAction action;

    public AttachementActionEvent(AttachmentEntity attachmentEntity, FileAction action) {
        this.attachmentEntity = attachmentEntity;
        this.action = action;
    }


}