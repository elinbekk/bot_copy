package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import java.util.List;

public interface LinkRepo {
    Long save(Link link);

    void delete(Long chatId, String url);

    boolean exists(Long chatId, String url);

    List<Link> findAllLinksByChatId(Long chatId);
}
