package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LinkCheckerScheduler {
    private static final Logger log = LoggerFactory.getLogger(LinkCheckerScheduler.class);
    private final BotClient botClient;
    private final GithubClient githubClient;
    private final StackOverflowClient stackoverflowClient;
    private final LinkRepository linkRepository;

    public LinkCheckerScheduler(
            BotClient botClient,
            GithubClient githubClient,
            StackOverflowClient stackoverflowClient,
            LinkRepository linkRepository) {
        this.botClient = botClient;
        this.githubClient = githubClient;
        this.stackoverflowClient = stackoverflowClient;
        this.linkRepository = linkRepository;
    }

    @Scheduled(fixedRateString = "${app.scheduler.interval-in-ms}")
    public void checkAllLinks() {
        try {
            Map<Link, Set<Long>> linksWithChats = linkRepository.findAllLinksWithChatIds();

            for (Map.Entry<Link, Set<Long>> entry : linksWithChats.entrySet()) {
                Link resource = entry.getKey();
                Set<Long> chatIds = entry.getValue();

                if (isUpdated(resource)) {
                    LinkUpdate update = new LinkUpdate(
                        resource.getUrl(),
                        "Обнаружены изменения",
                        new ArrayList<>(chatIds)
                    );
                    resource.setLastCheckedTime(String.valueOf(Instant.now()));
                    botClient.sendUpdateNotification(update);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка планировщика:", e);
        }
    }

    protected boolean isUpdated(Link resource) {
        return switch (resource.getLinkType()) {
            case GITHUB_REPO, GITHUB_ISSUE, GITHUB_PR -> githubClient.hasUpdates(resource);
            case STACKOVERFLOW -> stackoverflowClient.hasUpdates(resource);
        };
    }
}
