package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.DuplicateLinkException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepo;
import backend.academy.scrapper.service.LinkService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LinkServiceImpl implements LinkService {
    private final ChatRepository chatsRepo;
    private final LinkRepo linksRepo;

    public LinkServiceImpl(ChatRepository chats, LinkRepo links) {
        this.chatsRepo = chats;
        this.linksRepo = links;
    }

    public void addLink(Long chatId, Link link) {
        if (!chatsRepo.exists(chatId)) {
            throw new IllegalArgumentException("Чат не зарегистрирован");
        }
        if (linksRepo.exists(chatId, link.getUrl())) {
            throw new DuplicateLinkException();
        }
        link.setChatId(chatId);
        linksRepo.save(link);
    }

    public void removeLink(Long chatId, String url) {
        if (!chatsRepo.exists(chatId)) throw new IllegalArgumentException();
        linksRepo.delete(chatId, url);
    }

    public List<Link> getUserListLinks(Long chatId) {
        if (!chatsRepo.exists(chatId)) throw new IllegalArgumentException();
        return linksRepo.findAllLinksByChatId(chatId);
    }
}
