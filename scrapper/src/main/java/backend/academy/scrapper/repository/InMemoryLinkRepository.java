package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryLinkRepository implements LinkRepository {
    private final Map<Long, List<Link>> store = new ConcurrentHashMap<>();

    @Override
    public List<Link> findAllByChatId(Long chatId) {
        return store.getOrDefault(chatId, new ArrayList<>());
    }

    @Override
    public void add(Long chatId, Link link) {
        store.computeIfAbsent(chatId, id -> new ArrayList<>());
        List<Link> links = store.get(chatId);
        boolean exists = links.stream().anyMatch(l -> l.getUrl().equals(link.getUrl()));
        if (exists) throw new IllegalStateException("Ссылка уже отслеживается");
        links.add(link);
    }

    @Override
    public void remove(Long chatId, String url) {
        List<Link> links = store.get(chatId);
        if (links == null || !links.removeIf(l -> l.getUrl().equals(url))) {
            throw new IllegalStateException("Ссылка не найдена");
        }
    }
}
