package olfa.laarif.chatapp.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        return emitter;
    }

    public boolean isConnected(String userId) {
        return emitters.containsKey(userId);
    }

    public Optional<SseEmitter> getEmitter(String userId) {
        return Optional.ofNullable(emitters.get(userId));
    }

    public void remove(String userId) {
        emitters.remove(userId);
    }
}
