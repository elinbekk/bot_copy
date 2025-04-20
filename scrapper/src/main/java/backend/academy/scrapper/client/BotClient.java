package backend.academy.scrapper.client;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkUpdate;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import java.util.List;

public class BotClient {
    private final RestClient restClient;

    public BotClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void sendUpdateNotification(Link resource) {
        restClient.post()
            .uri("/api/updates")
            .body(new LinkUpdate(
                resource.getLinkId(), //todo
                resource.getUrl(),
                "Обнаружены изменения"
            ))
            .retrieve()
            .toBodilessEntity();
    }

    public  @Nullable List<Link> getTrackedResources() {
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
