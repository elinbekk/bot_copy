package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.entity.LinkEntity;
import backend.academy.scrapper.entity.UpdateEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import java.util.List;

public class OrmUpdateRepository implements UpdateRepository {
    private final UpdateEntityRepository repo;
    private final ObjectMapper om;

    public OrmUpdateRepository(UpdateEntityRepository repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    @Override
    public void save(Long linkId, JsonNode payload, Instant occurredAt) {
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
    public List<UpdateDto> findUnsents(int page, int size) {
        Page<UpdateEntity> p = repo.findBySentFalse(PageRequest.of(page,size, Sort.by("occurredAt")));
        return p.getContent().stream().map(this::toModel).toList();
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
