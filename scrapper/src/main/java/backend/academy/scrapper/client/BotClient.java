package backend.academy.scrapper.client;

import backend.academy.bot.entity.LinkUpdate;
import backend.academy.bot.entity.TrackedResource;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class BotClient {
    private final WebClient webClient;

    public BotClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<List<TrackedResource>> getResourcesToCheck(String interval) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/links")
                .queryParam("interval", interval)
                .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(new RuntimeException("Failed to get resources: " + response.statusCode()))
            )
            .bodyToMono(new ParameterizedTypeReference<>() {
            });
    }

    public Mono<Void> sendUpdateNotification(LinkUpdate update) {
        return webClient.post()
            .uri("/api/updates")
            .bodyValue(update)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(new RuntimeException("Failed to send update: " + response.statusCode()))
            )
            .toBodilessEntity()
            .then();
    }
}
