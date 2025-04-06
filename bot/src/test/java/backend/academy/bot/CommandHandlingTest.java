package backend.academy.bot;

import backend.academy.bot.entity.TrackedResource;
import backend.academy.bot.repository.TrackedResourceRepository;
import backend.academy.bot.service.BotService;
import backend.academy.bot.service.TrackedResourceService;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static backend.academy.bot.BotMessages.START_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommandHandlingTest {
    private BotService botService;
    private TrackedResourceService trackedResourceService;
    private CommandHandler commandHandler;
    private final long testChatId = 123L;

    @BeforeEach
    void setUp() {
        botService = mock(BotService.class);
        TrackedResourceRepository linkRepository = mock(TrackedResourceRepository.class);
        trackedResourceService = new TrackedResourceService(linkRepository, botService);

        commandHandler = new CommandHandler(
            botService,
            trackedResourceService
        );
    }

    @Test
    void handleStartCommandSendsMessageTest() {
        commandHandler.handleState(testChatId, "/start");
        verify(botService).sendMessage(eq(testChatId), eq(START_MESSAGE));
    }

    @Test
    void handleUnknownCommandSendsErrorMessageTest() {
        commandHandler.handleState(testChatId, "/unknown");
        verify(botService).sendMessage(eq(testChatId), contains("Неизвестная команда"));
    }

    @Test
    void listCommandFormattingTest() {
        TrackedResource resource = new TrackedResource();
        resource.setLink("https://example.com");
        resource.setTags(Set.of("tag"));
        resource.setFilters(Map.of("key", "value"));
        resource.setLastCheckedTime(Instant.parse("2023-01-01T00:00:00Z"));

        String result = trackedResourceService.formatResource(resource);

        assertTrue(result.contains("example.com"));
        assertTrue(result.contains("tag"));
        assertTrue(result.contains("key: value"));
        assertTrue(result.contains("2023-01-01T00:00:00Z"));
    }

}
