package backend.academy.bot;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceSavingTest {
    @Test
    void saveTrackedResourceTest() {
        LinkRepository repo = mock(LinkRepository.class);
        BotService botService = mock(BotService.class);
        CommandHandler handler = new CommandHandler(botService, repo);

        TrackedResource resource = new TrackedResource();
        resource.setLink("https://github.com/user/repo");
        resource.setTags(Set.of("bug", "feature"));
        resource.setFilters(Map.of("state", "open"));

        handler.saveTrackedResource(123L, resource);

        ArgumentCaptor<TrackedResource> captor = ArgumentCaptor.forClass(TrackedResource.class);
        verify(repo).addLink(eq(123L), captor.capture());

        TrackedResource saved = captor.getValue();
        assertAll(
            () -> Assertions.assertEquals("https://github.com/user/repo", saved.getLink()),
            () -> assertTrue(saved.getTags().containsAll(Set.of("bug", "feature"))),
            () -> Assertions.assertEquals("open", saved.getFilters().get("state"))
        );
    }

    @Test
    void fullTrackingWorkflowSuccessTest() {
        InMemoryLinkRepository repo = new InMemoryLinkRepository();
        BotService botService = mock(BotService.class);
        CommandHandler handler = new CommandHandler(botService, repo);

        // Act - Track
        handler.handleCommand(123L, "/track");
        handler.handleCommand(123L, "https://github.com/user/repo");
        handler.handleCommand(123L, "bug feature");
        handler.handleCommand(123L, "state:open");

        // Assert - Add
        List<TrackedResource> resources = repo.getLinks(123L);
        Assertions.assertEquals(1, resources.size());

        // Act - Untrack
        handler.handleCommand(123L, "/untrack");
        handler.handleCommand(123L, "https://github.com/user/repo");

        // Assert - Remove
        assertTrue(repo.getLinks(123L).isEmpty());
    }

    @Test
    void saveDuplicateLinkThrowsExceptionTest() {
        InMemoryLinkRepository repo = new InMemoryLinkRepository();
        BotService botService = mock(BotService.class);
        CommandHandler handler = new CommandHandler(botService, repo);
        TrackedResource resource1 = new TrackedResource();
        resource1.setLink("https://github.com/user/repo");
        resource1.setChatId(123L);
        TrackedResource resource2 = new TrackedResource();
        resource2.setLink("https://github.com/user/repo");
        resource2.setChatId(123L);
        handler.saveTrackedResource(123L, resource1);
        assertThrows(IllegalArgumentException.class, () -> handler.saveTrackedResource(123L, resource2));
    }
}
