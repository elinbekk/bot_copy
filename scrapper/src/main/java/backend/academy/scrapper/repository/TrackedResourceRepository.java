package backend.academy.scrapper.repository;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.entity.TrackedResource;

import java.time.Instant;
import java.util.List;

public interface TrackedResourceRepository {
    void addResource(long chatId, TrackedResource link);
    void deleteResource(long chatId, String url);
    List<TrackedResource> getResourcesByChatId(long chatId);
    List<TrackedResource> getAllLinks();
    boolean existsByChatIdAndLink(long chatId, String link);
    List<TrackedResource> findByLastCheckedBefore(Instant checkFrom, LinkType type);
    void updateLastChecked(String url, Instant now);
}
