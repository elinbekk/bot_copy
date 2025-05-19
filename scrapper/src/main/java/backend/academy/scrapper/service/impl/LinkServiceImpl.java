package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.DuplicateLinkException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.LinkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;

@Service
public class LinkServiceImpl implements LinkService {
    private final ChatRepository chatsRepo;
    private final LinkRepository linksRepo;

    public LinkServiceImpl(ChatRepository chats, LinkRepository links) {
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

    @Override
    public Page<Link> findDueLinks(Pageable page) {
        return linksRepo.findDueLinks(page);
    }

    @Override
    public void updateLastChecked(Long linkId, Timestamp when) {
        linksRepo.updateLastChecked(linkId, when);
    }

    @Override
    public Link findById(Long linkId) {
        return linksRepo.findLinkById(linkId);
    }
}
