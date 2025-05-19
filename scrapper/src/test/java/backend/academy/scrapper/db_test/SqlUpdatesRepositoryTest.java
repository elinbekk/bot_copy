package backend.academy.scrapper.db_test;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.dto.Link;
import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.repository.impl.SqlChatRepository;
import backend.academy.scrapper.repository.impl.SqlLinkRepository;
import backend.academy.scrapper.repository.impl.SqlUpdateRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SqlUpdatesRepositoryTest extends BaseSqlTest {
    private final Long CHAT_ID = 100L;
    private final String LINK_URL = "https://example.com";
    private Long savedLinkId;

    @PostConstruct
    void init() {
        chatRepository = new SqlChatRepository(jdbcTemplate);
        updateRepository = new SqlUpdateRepository(jdbcTemplate, objectMapper);
        linkRepository = new SqlLinkRepository(jdbcTemplate, objectMapper);
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM updates");
        jdbcTemplate.update("DELETE FROM links");
        jdbcTemplate.update("DELETE FROM chats");

        chatRepository.save(CHAT_ID);
        Link link = new Link(
                LINK_URL,
                CHAT_ID,
                LinkType.GITHUB_PR,
                Set.of(),
                Map.of(),
                Instant.now().toString());
        savedLinkId = linkRepository.save(link);
    }

    @Test
    void saveAndFindUpdateTest() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("testKey", "testValue");
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Timestamp occurredAt = Timestamp.from(now);
        updateRepository.save(savedLinkId, payload, occurredAt);

        List<UpdateDto> updates = updateRepository.findUnsent();
        assertThat(updates).hasSize(1);

        UpdateDto u = updates.get(0);
        assertThat(u.getLinkId()).isEqualTo(savedLinkId);
        assertThat(u.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(u.getPayload().get("testKey").asText()).isEqualTo("testValue");
        assertThat(u.isSent()).isFalse();
    }

    @Test
    void updateSentTest() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("x", "y");
        Timestamp time = Timestamp.from(Instant.now());
        updateRepository.save(savedLinkId, payload, time);

        List<UpdateDto> updates = updateRepository.findUnsent();
        assertThat(updates).hasSize(1);

        Long updateId = updates.getFirst().getId();
        updateRepository.markSent(List.of(updateId));

        boolean sentFlag =
                jdbcTemplate.queryForObject("SELECT sent FROM updates WHERE id = ?", Boolean.class, updateId);
        assertThat(sentFlag).isTrue();
    }
}
