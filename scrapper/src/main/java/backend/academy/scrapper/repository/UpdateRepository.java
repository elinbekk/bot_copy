package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.UpdateDto;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import java.util.List;

public interface UpdateRepository {
    void save(Long linkId, JsonNode payload, Timestamp occurredAt);
    List<UpdateDto> findUnsents(int page, int size);
    void markSent(List<Long> updateIds);
}

