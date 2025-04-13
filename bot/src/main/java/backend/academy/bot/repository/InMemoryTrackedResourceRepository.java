package backend.academy.bot.repository;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.entity.TrackedResource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InMemoryTrackedResourceRepository implements TrackedResourceRepository {
    private final Map<Long, List<TrackedResource>> storage = new HashMap<>();

    @Override
    public void addResource(long chatId, TrackedResource link) {
        storage.computeIfAbsent(chatId, k -> new ArrayList<>()).add(link);
    }

    @Override
    public void deleteResource(long chatId, String url) {
        List<TrackedResource> resources = storage.get(chatId);
        if (resources != null) {
            resources.removeIf(resource -> resource.getLink().equals(url));
        }
    }

    @Override
    public List<TrackedResource> getResourcesByChatId(long chatId) {
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

    @Override
    public List<TrackedResource> findByLastCheckedBefore(Instant checkFrom, LinkType type) {
        return storage.values().stream()
            .flatMap(List::stream)
            .filter(res -> res.getLastCheckedTime().isBefore(checkFrom))
            .filter(res -> type == null || res.getResourceType() == type)
            .toList();
    }

    @Override
    public void updateLastChecked(String url, Instant checkTime) {
        storage.values().stream()
            .flatMap(List::stream)
            .filter(res -> res.getLink().equals(url))
            .forEach(res -> res.setLastCheckedTime(checkTime));
    }
}
