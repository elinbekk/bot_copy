package backend.academy.scrapper.db_test;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.repository.SqlChatRepository;
import backend.academy.scrapper.repository.SqlLinkRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@JdbcTest
public class SqlLinkRepositoryTest {
    private SqlLinkRepository linkRepo;
    private SqlChatRepository chatRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    void setUp() {
        linkRepo = new SqlLinkRepository(jdbcTemplate, objectMapper);
        chatRepo = new SqlChatRepository(jdbcTemplate);
    }

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM links");
        jdbcTemplate.update("DELETE FROM chats");
    }

    private final Long CHAT_ID = 1L;
    private final String URL = "https://example.com";
    private final LinkType TYPE = LinkType.GITHUB_PR;
    private final Set<String> TAGS = Set.of("tag1", "tag2");
    private final Map<String, String> FILTERS = Map.of("key", "value");
    private final String LAST_CHECKED = Instant.now().minusSeconds(60).toString();

    private Link createTestLink() {
        return new Link(
            null,
            URL,
            CHAT_ID,
            TYPE,
            TAGS,
            FILTERS,
            LAST_CHECKED
        );
    }

    @Test
    void save_ShouldPersistLinkInDatabase() {
        Link link = createTestLink();

        chatRepo.save(link.getChatId());
        linkRepo.save(link);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url = ? AND chat_id = ?",
            Integer.class,
            URL,
            CHAT_ID
        );
        assertThat(count).isEqualTo(1);
    }
}
