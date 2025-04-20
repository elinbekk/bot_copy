package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import java.util.List;
import backend.academy.scrapper.entity.Link;
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

    public LinkCheckerScheduler(BotClient botClient, GithubClient githubClient, StackOverflowClient stackoverflowClient) {
        this.botClient = botClient;
        this.githubClient = githubClient;
        this.stackoverflowClient = stackoverflowClient;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void checkAllLinks() {
        try {
            List<Link> resources = botClient.getTrackedResources();
            for (Link resource : resources) {
                boolean isUpdated = isUpdated(resource);
                if (isUpdated) {
                    botClient.sendUpdateNotification(resource);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка планировщика:", e);
        }
    }

    private boolean isUpdated(Link resource) {
        return switch (resource.getLinkType()) {
            case GITHUB_REPO, GITHUB_ISSUE, GITHUB_PR -> githubClient.hasUpdates(resource);
            case STACKOVERFLOW -> stackoverflowClient.hasUpdates(resource);
        };
    }
}


