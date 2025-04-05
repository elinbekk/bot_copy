package backend.academy.bot;

import java.util.List;
import java.util.Map;
import java.util.Set;

import backend.academy.bot.entity.TrackedResource;
import backend.academy.bot.repository.InMemoryLinkRepository;
import backend.academy.bot.repository.LinkRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ResourceSavingTest {
    private BotService botService;
    private TrackedResourceService trackedResourceService;
    private LinkRepository linkRepository;
    private CommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        botService = mock(BotService.class);
        linkRepository = mock(LinkRepository.class);
        trackedResourceService = new TrackedResourceService(linkRepository, botService);
        commandHandler = new CommandHandler(botService, linkRepository, trackedResourceService);

    }

    @Test
    void saveTrackedResourceTest() {
        TrackedResource resource = new TrackedResource();
        resource.setLink("https://github.com/user/repo");
        resource.setTags(Set.of("bug", "feature"));
        resource.setFilters(Map.of("state", "open"));

        trackedResourceService.saveTrackedResource(123L, resource);

        ArgumentCaptor<TrackedResource> captor = ArgumentCaptor.forClass(TrackedResource.class);
        verify(linkRepository).addLink(eq(123L), captor.capture());

        TrackedResource saved = captor.getValue();
        assertAll(
            () -> Assertions.assertEquals("https://github.com/user/repo", saved.getLink()),
            () -> assertTrue(saved.getTags().containsAll(Set.of("bug", "feature"))),
            () -> Assertions.assertEquals("open", saved.getFilters().get("state"))
        );
    }

    @Test
    void saveDuplicateLinkThrowsExceptionTest() {
        TrackedResource resource1 = new TrackedResource();
        resource1.setLink("https://github.com/user/repo");
        resource1.setChatId(123L);

        TrackedResource resource2 = new TrackedResource();
        resource2.setLink("https://github.com/user/repo");
        resource2.setChatId(123L);

        trackedResourceService.saveTrackedResource(123L, resource1);
        assertThrows(IllegalArgumentException.class, () -> trackedResourceService.saveTrackedResource(123L, resource2));
    }

    @Test
    void fullTrackingWorkflowSuccessTest() {
        commandHandler.handleCommand(123L, "/track");
        commandHandler.handleCommand(123L, "https://github.com/user/repo");
        commandHandler.handleCommand(123L, "bug feature");
        commandHandler.handleCommand(123L, "state:open");

        List<TrackedResource> resources = linkRepository.getLinks(123L);
        Assertions.assertEquals(1, resources.size());

        commandHandler.handleCommand(123L, "/untrack");
        commandHandler.handleCommand(123L, "https://github.com/user/repo");

        assertTrue(linkRepository.getLinks(123L).isEmpty());
    }
}
