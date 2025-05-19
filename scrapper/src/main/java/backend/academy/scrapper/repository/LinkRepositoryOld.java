package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.Link;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LinkRepositoryOld {//todo: need to remove
    List<Link> findAllByChatId(Long chatId);

    void saveLink(Long chatId, Link link) throws IllegalStateException;

    void remove(Long chatId, String url) throws IllegalStateException;

    boolean linkIsExists(Long chatId, Link link);

    List<Link> getAllLinks();

    Map<Link, Set<Long>> findAllLinksWithChatIds();
}
