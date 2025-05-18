package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.LinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LinkCheckerScheduler {
    private static final Logger logger = LoggerFactory.getLogger(LinkCheckerScheduler.class);
    private final BotClient botClient;
    private final GithubClient githubClient;
    private final StackOverflowClient stackoverflowClient;
    private final LinkService linkService;
    private final ChatService chatService;

    public LinkCheckerScheduler(
        BotClient botClient,
        GithubClient githubClient,
        StackOverflowClient stackoverflowClient,
        LinkService linkService, ChatService chatService) {
        this.botClient = botClient;
        this.githubClient = githubClient;
        this.stackoverflowClient = stackoverflowClient;
        this.linkService = linkService;
        this.chatService = chatService;
    }

    @Scheduled(fixedRateString = "${app.scheduler.interval-in-ms}")
    public void checkAllLinks() {
        try {
            List<Long> chatIds = chatService.getChatIds();
            for(Long chatId : chatIds) {
                List<Link> links = linkService.getUserListLinks(chatId);
                for(Link link : links) {
                    logger.debug("Проверяемая ссылка: {} (Тип: {})", link.getUrl(), link.getLinkType());
                    if (isUpdated(link)) {
                        logger.info("Обновления обнаружены для: {}", link.getUrl());
                        LinkUpdate update =
                            new LinkUpdate(link.getUrl(), "Обнаружены изменения", new ArrayList<>(chatIds));
                        link.setLastCheckedTime(String.valueOf(Instant.now()));
                        logger.debug("Отправление обновлений по {} в {} чатов", link.getUrl(), chatIds.size());
                        botClient.sendUpdateNotification(update);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка планировщика:", e);
        }
    }

    protected boolean isUpdated(Link resource) {
        return switch (resource.getLinkType()) {
            case GITHUB_REPO, GITHUB_ISSUE, GITHUB_PR -> githubClient.hasUpdates(resource);
            case STACKOVERFLOW -> stackoverflowClient.hasUpdates(resource);
        };
    }
}
