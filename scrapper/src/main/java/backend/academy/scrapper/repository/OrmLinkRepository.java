package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkEntity;
import backend.academy.scrapper.entity.LinkType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "access-type", havingValue = "ORM")
public class OrmLinkRepository implements LinkRepo {
    private final LinkEntityRepository linkRepo;
    private final ObjectMapper om;

    public OrmLinkRepository(LinkEntityRepository linkRepo, ObjectMapper om) {
        this.linkRepo = linkRepo;
        this.om = om;
    }

    @Override
    public void save(Long chatId, Link link) {
        ChatEntity chat = new ChatEntity();
        chat.setId(chatId);
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
        linkRepo.save(linkEntity);
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
}
