package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.sql.Timestamp;
import java.util.List;

public interface LinkRepository {
    Long save(Link link);

    void delete(Long chatId, String url);

    boolean exists(Long chatId, String url);

    List<Link> findAllLinksByChatId(Long chatId);

    Page<Link> findDueLinks(Pageable page);

    void updateLastChecked(Long linkId, Timestamp when);

    Link findLinkById(Long linkId);
}
