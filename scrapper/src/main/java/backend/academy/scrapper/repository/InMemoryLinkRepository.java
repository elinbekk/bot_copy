package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryLinkRepository implements LinkRepository {
    private final Map<Long, List<Link>> store = new ConcurrentHashMap<>();

    @Override
    public List<Link> findAllByChatId(Long chatId) {
        return store.getOrDefault(chatId, new ArrayList<>());
    }

    @Override
    public void saveLink(Long chatId, Link link) {
        store.computeIfAbsent(chatId, id -> new ArrayList<>());
        List<Link> links = store.get(chatId);
        boolean exists = linkIsExists(chatId, link);
        if (exists) throw new IllegalArgumentException("Ссылка уже отслеживается");
        links.add(link);
    }

    @Override
    public void remove(Long chatId, String url) {
        List<Link> links = store.get(chatId);
        if (links == null || !links.removeIf(l -> l.getUrl().equals(url))) {
            throw new IllegalArgumentException("Ссылка не найдена");
        }
    }

    @Override
    public boolean linkIsExists(Long chatId, Link link) {
        List<Link> links = store.get(chatId);
        if (links == null) return false;
        return links.stream()
                .anyMatch(l -> l.getUrl().equals(link.getUrl())
                        && Objects.equals(l.getTags(), link.getTags())
                        && Objects.equals(l.getFilters(), link.getFilters()));
    }

    @Override
    public List<Link> getAllLinks() {
        return store.values().stream().flatMap(List::stream).toList();
    }

    @Override
    public Set<Long> findAllChatIds() {
        return store.keySet();
    }
}
