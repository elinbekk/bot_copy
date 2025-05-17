package backend.academy.scrapper.db_test;

import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.repository.SqlChatRepository;
import backend.academy.scrapper.repository.SqlLinkRepository;
import backend.academy.scrapper.repository.SqlUpdateRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class SqlUpdatesRepositoryTest extends BaseSqlTest {
    private final Long CHAT_ID = 100L;
    private final String LINK_URL = "https://example.com";
    private final Long linkId = 1L;

    @PostConstruct
    void setUp() {
        chatRepository = new SqlChatRepository(jdbcTemplate);
        updateRepository = new SqlUpdateRepository(jdbcTemplate, objectMapper);
        linkRepository = new SqlLinkRepository(jdbcTemplate, objectMapper);
    }

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM updates");
        jdbcTemplate.update("DELETE FROM links");
        jdbcTemplate.update("DELETE FROM chats");

        chatRepository.save(CHAT_ID);
        Link link = new Link(1L, LINK_URL, CHAT_ID, LinkType.GITHUB_PR, Set.of(), Map.of(), Instant.now().toString());
        linkRepository.save(link);

    }

    @Test
    void save_and_findUnsents_shouldWorkCorrectly() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("testKey", "testValue");
        Instant occurredAt = Instant.now();

        updateRepository.save(linkId, payload, occurredAt);

        List<UpdateDto> updates = updateRepository.findUnsents(0, 10);
        assertThat(updates).hasSize(1);

        UpdateDto u = updates.get(0);
        assertThat(u.getLinkId()).isEqualTo(linkId);
        assertThat(u.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(u.getPayload().get("testKey").asText()).isEqualTo("testValue");
        assertThat(u.isSent()).isFalse();
    }

    @Test
    void markSent_shouldUpdateSentFlag() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("x", "y");
        Instant time = Instant.now();
        updateRepository.save(linkId, payload, time);

        List<UpdateDto> updates = updateRepository.findUnsents(0, 10);
        assertThat(updates).hasSize(1);

        Long updateId = updates.get(0).getId();

        // Act
        updateRepository.markSent(List.of(updateId));

        // Assert
        Integer sentFlag = jdbcTemplate.queryForObject(
            "SELECT sent FROM updates WHERE id = ?",
            Integer.class,
            updateId
        );
        assertThat(sentFlag).isEqualTo(1); // true
    }
}

