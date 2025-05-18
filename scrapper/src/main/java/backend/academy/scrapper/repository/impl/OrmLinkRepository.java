package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkEntity;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.repository.LinkEntityRepository;
import backend.academy.scrapper.repository.LinkRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public class OrmLinkRepository implements LinkRepository {
    private final LinkEntityRepository linkRepo;
    private final ObjectMapper om;

    public OrmLinkRepository(LinkEntityRepository linkRepo, ObjectMapper om) {
        this.linkRepo = linkRepo;
        this.om = om;
    }

    @Override
    public Long save(Link link) {
        ChatEntity chat = new ChatEntity();
        chat.setId(link.getChatId());
        LinkEntity linkEntity = new LinkEntity();
        linkEntity.setChat(chat);
        linkEntity.setUrl(link.getUrl());
        linkEntity.setType(link.getLinkType().name());
        linkEntity.setLastChecked(Instant.parse(link.getLastCheckedTime())); // todo: check this
        try {
            linkEntity.setTags(om.writeValueAsString(link.getTags()));
            linkEntity.setFilters(om.writeValueAsString(link.getFilters()));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        return linkRepo.save(linkEntity).getId();
    }

    @Override
    public void delete(Long chatId, String url) {
        linkRepo.deleteByChatIdAndUrl(chatId, url);
    }

    @Override
    public boolean exists(Long chatId, String url) {
        return linkRepo.existsByChatIdAndUrl(chatId, url);
    }

    @Override
    public List<Link> findAllLinksByChatId(Long chatId) {
        return linkRepo.findByChatId(chatId).stream()
                .map(linkEntity -> {
                    try {
                        return new Link(
                                linkEntity.getId(),
                                linkEntity.getUrl(),
                                LinkType.valueOf(linkEntity.getType()),
                                om.readValue(linkEntity.getTags(), new TypeReference<>() {}),
                                om.readValue(linkEntity.getFilters(), new TypeReference<>() {}),
                                String.valueOf(linkEntity.getLastChecked())); // todo: check this fieldd
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    @Override
    public Page<Link> findDueLinks(Pageable pg) {
        Page<LinkEntity> page = linkRepo.findDueLinks(pg);
        List<Link> content = page.getContent().stream()
            .map(linkEntity -> {
                try {
                    return new Link(
                        linkEntity.getId(),
                        linkEntity.getUrl(),
                        LinkType.valueOf(linkEntity.getType()),
                        om.readValue(linkEntity.getTags(), new TypeReference<>() {}),
                        om.readValue(linkEntity.getFilters(), new TypeReference<>() {}),
                        String.valueOf(linkEntity.getLastChecked())
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }).toList();

        return new PageImpl<>(content, pg, page.getTotalElements());
    }

    @Override
    public void updateLastChecked(Long linkId, Timestamp when) {
        linkRepo.updateLastChecked(linkId, when);
    }
}
