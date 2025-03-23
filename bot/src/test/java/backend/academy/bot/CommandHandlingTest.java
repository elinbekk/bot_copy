package backend.academy.bot;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommandHandlingTest {
    @Test
    void handleUnknownCommandSendsErrorMessageTest() {
        BotService botService = mock(BotService.class);
        CommandHandler handler = new CommandHandler(botService, mock(LinkRepository.class));

        handler.handleCommand(123L, "/unknown");

        verify(botService).sendMessage(eq(123L), contains("Неизвестная команда"));
    }

    @Test
    void listCommandFormattingTest() {
        TrackedResource resource = new TrackedResource();
        resource.setLink("https://example.com");
        resource.setTags(Set.of("tag"));
        resource.setFilters(Map.of("key", "value"));
        resource.setLastCheckedTime(Instant.parse("2023-01-01T00:00:00Z"));

        String result = new CommandHandler(mock(BotService.class), mock(LinkRepository.class))
            .formatResource(resource);

        assertTrue(result.contains("example.com"));
        assertTrue(result.contains("tag"));
        assertTrue(result.contains("key: value"));
        assertTrue(result.contains("2023-01-01T00:00:00Z"));
    }

}
