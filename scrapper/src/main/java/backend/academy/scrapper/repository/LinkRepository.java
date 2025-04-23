package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LinkRepository {
    List<Link> findAllByChatId(Long chatId);

    void saveLink(Long chatId, Link link) throws IllegalStateException;

    void remove(Long chatId, String url) throws IllegalStateException;

    boolean linkIsExists(Long chatId, Link link);

    List<Link> getAllLinks();

    Set<Long> findAllChatIds();

    Map<Link, Set<Long>> findAllLinksWithChatIds();
}
