package backend.academy.scrapper;

import static backend.academy.scrapper.entity.LinkType.GITHUB_REPO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.dto.Link;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.Map;
import java.util.Set;
import backend.academy.scrapper.repository.UpdateRepository;
import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UpdateService;
import backend.academy.scrapper.service.impl.ChatServiceImpl;
import backend.academy.scrapper.service.impl.LinkServiceImpl;
import backend.academy.scrapper.service.impl.UpdateServiceImpl;
import org.junit.Test;
import org.mockito.Mockito;

public class SchedulerTest {
    private final ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
    private final LinkRepository linkRepository = Mockito.mock(LinkRepository.class);
    private final UpdateRepository updateRepository = Mockito.mock(UpdateRepository.class);
    private final LinkService linkService = new LinkServiceImpl(chatRepository, linkRepository);
    private final ChatService chatService = new ChatServiceImpl(chatRepository);
    private final UpdateService updateService = new UpdateServiceImpl(updateRepository);
    private final BotClient botClient = Mockito.mock(BotClient.class);
    private final GithubClient githubClient = Mockito.mock(GithubClient.class);
    private final StackOverflowClient soClient = Mockito.mock(StackOverflowClient.class);
    int pageSize = 5;
    private final LinkCheckerScheduler linkScheduler =
            new LinkCheckerScheduler(githubClient, soClient, linkService, updateService, pageSize);

    private static final long testChatId = 123L;
    private static final long testChatId2 = 124L;
    private static final long testLinkId = 1234L;
    private static final long testLinkId2 = 1234L;

    private final Link link1 = new Link(
            testLinkId,
            "https://github.com/user/repo",
            GITHUB_REPO,
            Set.of("bug", "feature"),
            Map.of("state", "open"),
            "2025-04-16T12:00:00Z");

    private final Link link2 = new Link(
            testLinkId2,
            "https://github.com/user2/repo2",
            GITHUB_REPO,
            Set.of("bug", "feature"),
            Map.of("state", "open"),
            "2025-04-16T12:00:00Z");

    @Test
    public void shouldSendNotificationsOnlySubscribersWhenLinksUpdated() {
        chatService.registerChat(testChatId);
        chatService.registerChat(testChatId2);
        linkService.addLink(testChatId, link1);
        linkService.addLink(testChatId, link2);
        linkService.addLink(testChatId2, link1);

        when(githubClient.hasUpdates(any(Link.class))).thenReturn(true);

        linkScheduler.checkAllLinks();

        verify(botClient)
                .sendUpdateNotification(argThat(update -> update.getLink().equals(link1.getUrl())
                        && update.getDescription().equals("Обнаружены изменения")
                        && update.getTgChatIds().containsAll(Set.of(testChatId, testChatId2))));
    }
}
