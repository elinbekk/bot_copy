package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import java.util.List;

public interface LinkRepository {
    List<Link> findAllByChatId(Long chatId);
    void add(Long chatId, Link link) throws IllegalStateException;
    void remove(Long chatId, String url) throws IllegalStateException;
}
