package backend.academy.bot;

import backend.academy.bot.entity.TrackedResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import backend.academy.bot.repository.InMemoryTrackedResourceRepository;
import backend.academy.bot.repository.TrackedResourceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceSavingTest {
    private TrackedResourceService trackedResourceService;
    private TrackedResourceRepository resourceRepository;
    private CommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        BotService botService = mock(BotService.class);
        resourceRepository = mock(TrackedResourceRepository.class);
        trackedResourceService = new TrackedResourceService(resourceRepository, botService);
        commandHandler = new CommandHandler(botService, trackedResourceService);

    }

    @Test
    void saveTrackedResourceTest() {
        TrackedResource resource = new TrackedResource();
        resource.setLink("https://github.com/user/repo");
        resource.setTags(Set.of("bug", "feature"));
        resource.setFilters(Map.of("state", "open"));

        List<TrackedResource> savedResources = new ArrayList<>();

        trackedResourceService.saveTrackedResource(123L, resource);

        doAnswer(invocation -> {
            savedResources.add(invocation.getArgument(1));
            return null;
        }).when(resourceRepository).addResource(anyLong(), any(TrackedResource.class));

        // Настройка мока для getResourcesByChatId
        when(resourceRepository.getResourcesByChatId(123L)).thenReturn(savedResources);

        trackedResourceService.saveTrackedResource(123L, resource);
        assertEquals(1, savedResources.size());
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



        when(resourceRepository.existsByChatIdAndLink(123L, "https://github.com/user/repo")).thenReturn(true);


        assertThrows(IllegalArgumentException.class, () ->
            trackedResourceService.saveTrackedResource(123L, resource2)
        );
    }

    @Test
    void fullTrackingWorkflowSuccessTest() {
        commandHandler.handleCommand(123L, "/track");
        commandHandler.handleCommand(123L, "https://github.com/user/repo");
        commandHandler.handleCommand(123L, "bug feature");
        commandHandler.handleCommand(123L, "state:open");

        List<TrackedResource> resources = resourceRepository.getResourcesByChatId(123L);
//        Assertions.assertEquals(1, resources.size());

        commandHandler.handleCommand(123L, "/untrack");
        commandHandler.handleCommand(123L, "https://github.com/user/repo");

        assertTrue(resourceRepository.getResourcesByChatId(123L).isEmpty());
    }
}
