package backend.academy.scrapper.client;

import backend.academy.bot.dto.LinkUpdate;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import java.util.List;

public class BotClient {
    private final RestClient restClient;

    public BotClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void sendUpdateNotification(TrackedResource resource) {
        restClient.post()
            .uri("/api/updates")
            .body(new LinkUpdate(
                resource.getChatId(),
                resource.getLink(),
                "Обнаружены изменения"
            ))
            .retrieve()
            .toBodilessEntity();
    }

    public  @Nullable List<TrackedResource> getTrackedResources() {
        return restClient.delete()
            .uri(uriBuilder -> uriBuilder
                .path("/api/links")
                .queryParam("interval", "5m")
                .build())
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });
    }
}
