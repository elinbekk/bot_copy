package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.DuplicateLinkException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepo;

public class LinkService {
    private final ChatRepository chatsRepo;
    private final LinkRepo linksRepo;

    public LinkService(ChatRepository chats, LinkRepo links) {
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
        linksRepo.save(link);
    }
}
