package backend.academy.bot.repository;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.entity.TrackedResource;

import java.time.Instant;
import java.util.List;

public interface LinkRepository {
    void addLink(long chatId, TrackedResource link);
    void removeLink(long chatId, String url);
    List<TrackedResource> getLinks(long chatId);
    List<TrackedResource> getAllLinks();
    boolean existsByChatIdAndLink(long chatId, String link);
    List<TrackedResource> findByLastCheckedBefore(Instant checkFrom, LinkType type);
    void updateLastChecked(String url, Instant now);
}
