package backend.academy.scrapper.repository;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryChatRepository implements ChatRepository {
    private final Set<Long> chats = ConcurrentHashMap.newKeySet();

    @Override
    public void save(Long chatId) {
        chats.add(chatId);
    }

    @Override
    public boolean exists(Long chatId) {
        return chats.contains(chatId);
    }

    @Override
    public void delete(Long chatId) {
        chats.remove(chatId);
    }
}
