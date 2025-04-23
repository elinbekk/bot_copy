package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.mockito.Mockito;
import static backend.academy.scrapper.entity.LinkType.GITHUB_REPO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulerTest {
    private final LinkRepository linkRepository = new InMemoryLinkRepository();
    private final BotClient botClient = Mockito.mock(BotClient.class);
    private final GithubClient githubClient = Mockito.mock(GithubClient.class);
    private final StackOverflowClient soClient = Mockito.mock(StackOverflowClient.class);
    private final LinkCheckerScheduler linkScheduler = new LinkCheckerScheduler(botClient, githubClient, soClient, linkRepository);

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
        linkRepository.saveLink(testChatId, link1);
        linkRepository.saveLink(testChatId, link2);
        linkRepository.saveLink(testChatId2, link1);

        when(githubClient.hasUpdates(any(Link.class)))
            .thenReturn(true);

        linkScheduler.checkAllLinks();

        verify(botClient).sendUpdateNotification(argThat(update ->
            update.getLink().equals(link1.getUrl()) &&
                update.getDescription().equals("Обнаружены изменения") &&
                update.getTgChatIds().containsAll(Set.of(testChatId, testChatId2))
        ));
    }
}
