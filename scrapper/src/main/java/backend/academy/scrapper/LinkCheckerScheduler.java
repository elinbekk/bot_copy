package backend.academy.scrapper;

import backend.academy.bot.entity.LinkUpdate;
import backend.academy.bot.entity.TrackedResource;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LinkCheckerScheduler {
    private static final Logger log = LoggerFactory.getLogger(LinkCheckerScheduler.class);
    private final WebClient botClient;
    private final GithubClient githubClient;
    private final StackOverflowClient stackoverflowClient;

    public LinkCheckerScheduler(WebClient botClient, GithubClient githubClient, StackOverflowClient stackoverflowClient) {
        this.botClient = botClient;
        this.githubClient = githubClient;
        this.stackoverflowClient = stackoverflowClient;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void checkAllLinks() {
        try {
            List<TrackedResource> resources = getTrackedResources();
            for (TrackedResource resource : resources) {
                boolean isUpdated = isUpdated(resource);
                if (isUpdated) {
                    sendUpdateNotification(resource);
                }
            }
        } catch (Exception e) {
            log.error("Scheduler error", e);
        }
    }

    private void sendUpdateNotification(TrackedResource resource) {
        botClient.post()
            .uri("/api/updates")
            .bodyValue(new LinkUpdate(
                resource.getChatId(),
                resource.getLink(),
                "Обнаружены изменения"
            ))
            .retrieve()
            .toBodilessEntity()
            .block();
    }

    private boolean isUpdated(TrackedResource resource) {
        return switch (resource.getResourceType()) {
            case GITHUB_REPO, GITHUB_ISSUE, GITHUB_PR -> githubClient.hasUpdates(resource);
            case STACKOVERFLOW -> stackoverflowClient.hasUpdates(resource);
        };
    }

    private @Nullable List<TrackedResource> getTrackedResources() {
        return botClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/links")
                .queryParam("interval", "5m")
                .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<TrackedResource>>() {
            })
            .block();
    }
}


