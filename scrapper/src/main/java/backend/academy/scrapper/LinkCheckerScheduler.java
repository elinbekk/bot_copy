package backend.academy.scrapper;

import backend.academy.bot.LinkUpdate;
import backend.academy.bot.TrackedResource;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class LinkCheckerScheduler {
    private final WebClient botClient;
    private final GithubClient githubClient;
    private final StackOverflowClient stackoverflowClient;

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
                case GITHUB -> githubClient.hasUpdates(resource.getLink(), resource.getLastCheckedTime());
                case STACKOVERFLOW -> stackoverflowClient.hasUpdates(resource.getLink(), resource.getLastCheckedTime());
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


