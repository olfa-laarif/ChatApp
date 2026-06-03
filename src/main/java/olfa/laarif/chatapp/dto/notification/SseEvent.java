package olfa.laarif.chatapp.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SseEvent<T> {

    private String type;
    private T payload;
}
