package backend.academy.scrapper;

import backend.academy.bot.entity.LinkUpdate;
import backend.academy.bot.entity.TrackedResource;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LinkCheckerScheduler {
    private final WebClient botClient;
    private final GithubClient githubClient;
    private final StackOverflowClient stackoverflowClient;
    private static final Logger log = LoggerFactory.getLogger(LinkCheckerScheduler.class);

    public LinkCheckerScheduler(WebClient botClient, GithubClient githubClient, StackOverflowClient stackoverflowClient) {
        this.botClient = botClient;
        this.githubClient = githubClient;
        this.stackoverflowClient = stackoverflowClient;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void checkAllLinks() {
        List<TrackedResource> resources = botClient.get()
            .uri("/api/links?interval=5m")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<TrackedResource>>() {
            })
            .block();

        resources.forEach(resource -> {
            boolean isUpdated = switch (resource.getLinkType()) {
                case GITHUB_REPO, GITHUB_ISSUE, GITHUB_PR -> githubClient.hasUpdates(resource);
                case STACKOVERFLOW -> stackoverflowClient.hasUpdates(resource);
            };

            if (isUpdated) {
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
        });
    }
}


