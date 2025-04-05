package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.repository.TrackedResourceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

public class LinkParserTest {
    private TrackedResourceService trackedResourceService;

    @BeforeEach
    void setUp() {
        BotService botService = mock(BotService.class);
        TrackedResourceRepository linkRepository = mock(TrackedResourceRepository.class);
        trackedResourceService = new TrackedResourceService(linkRepository, botService);
    }

    @Test
    void linkTypeCorrectDetectedTest() {
        assertAll(
            () -> Assertions.assertEquals(LinkType.GITHUB_REPO, trackedResourceService.detectResourceType("https://github.com/user/repo")),
            () -> Assertions.assertEquals(LinkType.STACKOVERFLOW, trackedResourceService.detectResourceType("https://stackoverflow.com/questions/123")),
            () -> assertThrows(IllegalArgumentException.class,
                () -> trackedResourceService.detectResourceType("https://google.com"))
        );
    }
}
