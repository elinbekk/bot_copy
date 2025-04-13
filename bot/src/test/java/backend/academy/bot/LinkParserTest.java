package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.repository.TrackedResourceRepository;
import backend.academy.bot.service.BotService;
import backend.academy.bot.service.TrackedResourceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

public class LinkParserTest {
    private ResourceTypeDetector resourceTypeDetector;

    @BeforeEach
    void setUp() {
        resourceTypeDetector = new ResourceTypeDetector();
    }

    @Test
    void linkTypeCorrectDetectedTest() {
        assertAll(
            () -> Assertions.assertEquals(LinkType.GITHUB_REPO, resourceTypeDetector.detectResourceType("https://github.com/user/repo")),
            () -> Assertions.assertEquals(LinkType.GITHUB_ISSUE, resourceTypeDetector.detectResourceType("https://github.com/user/repo/issues/123")),
            () -> Assertions.assertEquals(LinkType.GITHUB_PR, resourceTypeDetector.detectResourceType("https://github.com/user/repo/pull/4")),
            () -> Assertions.assertEquals(LinkType.STACKOVERFLOW, resourceTypeDetector.detectResourceType("https://stackoverflow.com/questions/123")),
            () -> assertThrows(IllegalArgumentException.class,
                () -> resourceTypeDetector.detectResourceType("https://google.com"))
        );
    }
}
