package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.helper.InputParser;
import backend.academy.bot.helper.LinkTypeDetector;
import backend.academy.bot.service.BotService;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandHandlingTest {
    private static BotService botService;
    private static CommandHandler commandHandler;

    private static final long testChatId = 123L;

    @BeforeEach
    void setUp() {
        botService = mock(BotService.class);
        ScrapperClient scrapperClient = mock(ScrapperClient.class);
        LinkTypeDetector resourceTypeDetector = new LinkTypeDetector();
        InputParser inputParser = new InputParser();

        commandHandler = new CommandHandler(botService, scrapperClient, inputParser, resourceTypeDetector);
    }

    @Test
    void handleStartCommandSendsMessageTest() {
        final String START_MESSAGE =
                """
            Привет! Я помогу отслеживать изменения на GitHub и Stack Overflow.
            Доступные команды:
            /track - начать отслеживание ссылки
            /untrack - прекратить отслеживание
            /list - показать отслеживаемые ссылки
            /help - показать справку""";

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
        LinkResponse resource =
                new LinkResponse("https://example.com", Set.of("tag"), Map.of("key", "value"), "2023-01-01T00:00:00Z");

        String result = commandHandler.formatLink(resource);

        assertTrue(result.contains("example.com"));
        assertTrue(result.contains("tag"));
        assertTrue(result.contains("key: value"));
        assertTrue(result.contains("01.01.2023 03:00:00 MSK"));
    }
}
