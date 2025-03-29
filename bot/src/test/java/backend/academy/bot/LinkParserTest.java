package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.repository.LinkRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

public class LinkParserTest {
    @Test
    void linkTypeCorrectDetectedTest() {
        CommandHandler handler = new CommandHandler(mock(BotService.class), mock(LinkRepository.class));
        assertAll(
            () -> Assertions.assertEquals(LinkType.GITHUB, handler.detectLinkType("https://github.com/user/repo")),
            () -> Assertions.assertEquals(LinkType.STACKOVERFLOW, handler.detectLinkType("https://stackoverflow.com/questions/123")),
            () -> assertThrows(IllegalArgumentException.class,
                () -> handler.detectLinkType("https://google.com"))
        );
    }
}
