package backend.academy.bot;


import backend.academy.bot.service.BotService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceSavingTest {
    /*rivate CommandHandler commandHandler;

    private static final long testChatId = 123L;

    @BeforeEach
    void setUp() {
        BotService botService = mock(BotService.class);
        resourceRepository = mock(TrackedResourceRepository.class);

        trackedResourceService = new TrackedResourceService(resourceRepository, botService);
        LinkTypeDetector resourceTypeDetector = new LinkTypeDetector();
        InputParser inputParser = new InputParser();

        commandHandler = new CommandHandler(botService, inputParser, trackedResourceService, resourceTypeDetector);

    }

    @Test
    void saveTrackedResourceTest() {
        TrackedResource resource = new TrackedResource();
        resource.setLink("https://github.com/user/repo");
        resource.setTags(Set.of("bug", "feature"));
        resource.setFilters(Map.of("state", "open"));

        List<TrackedResource> savedResources = new ArrayList<>();

        trackedResourceService.saveTrackedResource(testChatId, resource);

        doAnswer(invocation -> {
            savedResources.add(invocation.getArgument(1));
            return null;
        }).when(resourceRepository).addResource(anyLong(), any(TrackedResource.class));

        when(resourceRepository.getResourcesByChatId(testChatId)).thenReturn(savedResources);

        trackedResourceService.saveTrackedResource(testChatId, resource);
        assertEquals(1, savedResources.size());
    }

    @Test
    void saveDuplicateLinkThrowsExceptionTest() {
        TrackedResource resource1 = new TrackedResource();
        resource1.setLink("https://github.com/user/repo");
        resource1.setChatId(testChatId);

        TrackedResource resource2 = new TrackedResource();
        resource2.setLink("https://github.com/user/repo");
        resource2.setChatId(testChatId);

        trackedResourceService.saveTrackedResource(testChatId, resource1);
        when(resourceRepository.existsByChatIdAndLink(testChatId, "https://github.com/user/repo")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            trackedResourceService.saveTrackedResource(testChatId, resource2)
        );
    }

    @Test
    void fullTrackingWorkflowSuccessTest() {
        commandHandler.handleState(testChatId, "/track");
        commandHandler.handleState(testChatId, "https://github.com/user/repo");
        commandHandler.handleState(testChatId, "bug feature");
        commandHandler.handleState(testChatId, "state:open");

        List<TrackedResource> resources = resourceRepository.getResourcesByChatId(testChatId);

        commandHandler.handleState(testChatId, "/untrack");
        commandHandler.handleState(testChatId, "https://github.com/user/repo");

        assertTrue(resourceRepository.getResourcesByChatId(testChatId).isEmpty());
    }*/
}
