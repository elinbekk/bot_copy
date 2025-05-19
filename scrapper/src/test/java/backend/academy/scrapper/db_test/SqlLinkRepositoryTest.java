package backend.academy.scrapper.db_test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import backend.academy.scrapper.dto.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.repository.impl.SqlChatRepository;
import backend.academy.scrapper.repository.impl.SqlLinkRepository;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SqlLinkRepositoryTest extends BaseSqlTest {
    @PostConstruct
    void init() {
        linkRepository = new SqlLinkRepository(jdbcTemplate, objectMapper);
        chatRepository = new SqlChatRepository(jdbcTemplate);
    }

    @BeforeEach
    void clean() {
        jdbcTemplate.update("DELETE FROM links");
        jdbcTemplate.update("DELETE FROM chats");
    }

    private final Long CHAT_ID = 1L;
    private final String URL = "https://example.com";
    private final LinkType TYPE = LinkType.GITHUB_PR;
    private final Set<String> TAGS = Set.of("tag");
    private final Map<String, String> FILTERS = Map.of("key", "value");
    private final String LAST_CHECKED = Instant.now().minusSeconds(60).toString();

    private Link createTestLink() {
        return new Link(null, URL, CHAT_ID, TYPE, TAGS, FILTERS, LAST_CHECKED);
    }

    @Test
    void saveInDatabaseTest() {
        Link link = createTestLink();

        chatRepository.save(link.getChatId());
        linkRepository.save(link);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM links WHERE url = ? AND chat_id = ?", Integer.class, URL, CHAT_ID);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteFromDatabaseTest() {
        Link link = createTestLink();
        chatRepository.save(link.getChatId());
        linkRepository.save(link);
        linkRepository.delete(CHAT_ID, URL);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM links WHERE url = ? AND chat_id = ?", Integer.class, URL, CHAT_ID);
        assertThat(count).isEqualTo(0);
    }

    @Test
    void linkIsExistsTest() {
        Link link = createTestLink();
        chatRepository.save(link.getChatId());
        linkRepository.save(link);
        boolean exists = linkRepository.exists(CHAT_ID, URL);
        Assertions.assertTrue(exists);
    }

    @Test
    void saveSerializeJsonFieldsTest() {
        Link link = createTestLink();
        chatRepository.save(link.getChatId());
        linkRepository.save(link);
        Map<String, Object> saved = jdbcTemplate.queryForMap("SELECT tags, filters FROM links WHERE url = ?", URL);

        assertThat(saved.get("tags")).isEqualTo("[\"tag\"]");
        assertThat(saved.get("filters")).isEqualTo("{\"key\":\"value\"}");
    }
}
