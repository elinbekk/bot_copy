package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.entity.LinkEntity;
import backend.academy.scrapper.entity.UpdateEntity;
import backend.academy.scrapper.repository.UpdateEntityRepository;
import backend.academy.scrapper.repository.UpdateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public class OrmUpdateRepository implements UpdateRepository {
    private final UpdateEntityRepository repo;
    private final ObjectMapper om;

    public OrmUpdateRepository(UpdateEntityRepository repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    @Override
    public void save(Long linkId, JsonNode payload, Timestamp occurredAt) {
        UpdateEntity e = new UpdateEntity();
        e.setOccurredAt(occurredAt);
        try {
            e.setPayload(om.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        e.setSent(false);
        e.setLink(new LinkEntity(linkId));
        repo.save(e);
    }

    @Override
    public List<UpdateDto> findUnsent() {
        List<UpdateEntity> p = repo.findBySentFalse();
        return p.stream().map(this::toModel).toList();
    }

    @Override
    public void markSent(List<Long> updateIds) {
        repo.findAllById(updateIds).forEach(e -> e.setSent(true));
        repo.saveAll(repo.findAllById(updateIds));
    }

    private UpdateDto toModel(UpdateEntity e) {
        UpdateDto u = new UpdateDto();
        u.setId(e.getId());
        u.setLinkId(e.getLink().getId());
        u.setOccurredAt(e.getOccurredAt());
        u.setSent(e.isSent());
        try {
            u.setPayload(om.readTree(e.getPayload()));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        return u;
    }
}
