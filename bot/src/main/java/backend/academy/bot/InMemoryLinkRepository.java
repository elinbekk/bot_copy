package backend.academy.bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InMemoryLinkRepository implements LinkRepository {
    private final Map<Long, List<TrackedResource>> storage = new HashMap<>();

    @Override
    public void addLink(long chatId, TrackedResource link) {
        storage.computeIfAbsent(chatId, k -> new ArrayList<>()).add(link);
    }

    @Override
    public void removeLink(long chatId, String url) {
        List<TrackedResource> resources = storage.get(chatId);
        if (resources != null) {
            resources.removeIf(resource -> resource.getLink().equals(url));
        }
    }

    @Override
    public List<TrackedResource> getLinks(long chatId) {
        return storage.getOrDefault(chatId, Collections.emptyList());
    }

    @Override
    public List<TrackedResource> getAllLinks() {
        return storage.values().stream()
            .flatMap(List::stream)
            .toList();
    }

    @Override
    public boolean existsByChatIdAndLink(long chatId, String link) {
        return storage.values().stream()
            .flatMap(List::stream)
            .anyMatch(r -> r.getChatId() == chatId && r.getLink().equals(link));

    }
}
