package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.Link;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LinkService {
    void addLink(Long chatId, Link link);

    void removeLink(Long chatId, String url);

    List<Link> getUserListLinks(Long chatId);

    Page<Link> findDueLinks(Pageable page);

    void updateLastChecked(Long linkId, Timestamp when);

    Link findById(Long linkId);
}
