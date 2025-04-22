package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.List;
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

    @Scheduled(fixedRate = 2 * 60 * 1000)
    public void checkAllLinks() {
        try {
            Set<Long> allChatIDs = linkRepository.findAllChatIds();
            for (Long chatID : allChatIDs) {
                List<Link> resources = linkRepository.findAllByChatId(chatID);
                for (Link resource : resources) {
                    boolean isUpdated = isUpdated(resource);
                    if (isUpdated) {
                        LinkUpdate upd = new LinkUpdate(resource.getUrl(), "Обнаружены изменения", List.of(chatID));
                        botClient.sendUpdateNotification(upd);
                    }
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
