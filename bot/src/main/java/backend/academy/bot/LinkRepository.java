package backend.academy.bot;

import java.util.List;

public interface LinkRepository {
    void addLink(long chatId, TrackedResource link);
    void removeLink(long chatId, String url);
    List<TrackedResource> getLinks(long chatId);
    List<TrackedResource> getAllLinks();
    boolean existsByChatIdAndLink(long chatId, String link);
}
