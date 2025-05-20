package backend.academy.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.dto.Link;
import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.entity.LinkType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NotificationMessageFormatTest {
    private NotificationSender sender;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        sender = new NotificationSender(null, null, null);
        mapper = new ObjectMapper();
    }

    @Test
    void formatGithubMessage() {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("title", "My Repo");
        payload.put("user", "alice");
        payload.put("createdAt", "2025-05-19T12:00:00Z");
        payload.put("preview", "Hello world");

        UpdateDto dto = new UpdateDto(1L, Timestamp.from(Instant.now()), payload, false);

        Link link = new Link(
                1L, "https://github.com/x", 42L, LinkType.GITHUB_PR, Set.of(), Map.of(), "2025-05-19T11:00:00Z");

        String text = sender.formatMessage(dto, link.getLinkType());

        assertThat(text).isEqualTo("My Repo\n" + "Автор: alice\n" + "Время: 2025-05-19T12:00:00Z\n" + "Hello world...");
    }

    @Test
    void formatStackOverflowMessage() {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("questionTitle", "How to X?");
        payload.put("user", "bob");
        payload.put("createdAt", "2025-05-19T13:00:00Z");
        payload.put("preview", "Answer is");

        UpdateDto dto = new UpdateDto(2L, Timestamp.from(Instant.now()), payload, false);

        Link link = new Link(
                2L,
                "https://stackoverflow.com/questions/123",
                43L,
                LinkType.STACKOVERFLOW,
                Set.of(),
                Map.of(),
                "2025-05-19T12:00:00Z");

        String text = sender.formatMessage(dto, link.getLinkType());

        assertThat(text)
                .isEqualTo("Вопрос: How to X?\n" + "От: bob\n" + "Время: 2025-05-19T13:00:00Z\n" + "Answer is...");
    }
}
