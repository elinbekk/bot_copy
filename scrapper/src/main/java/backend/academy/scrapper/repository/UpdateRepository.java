package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.UpdateDto;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;

public interface UpdateRepository {
    void save(Long linkId, JsonNode payload, Instant occurredAt);
    List<UpdateDto> findUnsents(int page, int size);
    void markSent(List<Long> updateIds);
}
