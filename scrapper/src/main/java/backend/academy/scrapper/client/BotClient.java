package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
public class BotClient {
    private final RestClient restClient;

    public BotClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void sendUpdateNotification(LinkUpdate linkUpdate) {
        try {
            restClient.post()
                .uri("/updates")
                .body(linkUpdate)
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Chat Id: {}{}", linkUpdate.getTgChatIds(), e.getMessage());
        }
    }
}
