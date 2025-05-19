package backend.academy.scrapper;

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
import static org.assertj.core.api.Assertions.assertThat;

public class NotificationMessageFormatTest {

    private NotificationSender sender;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        // Для тестирования формата нам не нужны реальные сервисы
        sender = new NotificationSender(null, null, null);
        mapper = new ObjectMapper();
    }

    @Test
    void formatGithubMessage() {
        // готовим DTO с payload
        ObjectNode payload = mapper.createObjectNode();
        payload.put("title", "My Repo");
        payload.put("user", "alice");
        payload.put("createdAt", "2025-05-19T12:00:00Z");
        payload.put("preview", "Hello world");

        UpdateDto dto = new UpdateDto(
            1L,                      // linkId
            Timestamp.from(Instant.now()),           // occurredAt
            payload,
            false
        );

        Link link = new Link(
            1L,
            "https://github.com/x", // id
            42L,                     // chatId
            // url
            LinkType.GITHUB_REPO,
            Set.of(), Map.of(),
            "2025-05-19T11:00:00Z"   // lastCheckedTime
        );

        String text = sender.formatMessage(dto);

        assertThat(text).isEqualTo(
            "My Repo\n" +
                "Автор: alice\n" +
                "Время: 2025-05-19T12:00:00Z\n" +
                "Hello world…"
        );
    }

    @Test
    void formatStackOverflowMessage() {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("questionTitle", "How to X?");
        payload.put("user", "bob");
        payload.put("createdAt", "2025-05-19T13:00:00Z");
        payload.put("preview", "Answer is...");

        UpdateDto dto = new UpdateDto(
            2L,
            Timestamp.from(Instant.now()),
            payload,
            false
        );

        Link link = new Link(
            2L,
            "https://stackoverflow.com/questions/123",
            43L,
            LinkType.STACKOVERFLOW,
            Set.of(), Map.of(),
            "2025-05-19T12:00:00Z"
        );

        String text = sender.formatMessage(dto);

        assertThat(text).isEqualTo(
            "Вопрос: How to X?\n" +
                "От: bob\n" +
                "Время: 2025-05-19T13:00:00Z\n" +
                "Answer is…"
        );
    }
}
