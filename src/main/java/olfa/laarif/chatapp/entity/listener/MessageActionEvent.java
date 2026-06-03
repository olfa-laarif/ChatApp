package olfa.laarif.chatapp.entity.listener;

import lombok.Getter;
import olfa.laarif.chatapp.entity.MessageEntity;
import olfa.laarif.chatapp.enums.MessageAction;

@Getter
public class MessageActionEvent {
    private final MessageEntity message;
    private final MessageAction action;

    public MessageActionEvent(MessageEntity message, MessageAction action) {
        this.message = message;
        this.action = action;
    }
}