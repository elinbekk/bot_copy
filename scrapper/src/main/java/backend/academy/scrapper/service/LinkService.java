package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Link;
import java.util.List;

public interface LinkService {
    void addLink(Long chatId, Link link);
    void removeLink(Long chatId, String url);
    List<Link> getUserListLinks(Long chatId);
}
