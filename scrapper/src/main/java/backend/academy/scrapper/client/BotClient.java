package backend.academy.scrapper.client;

import static backend.academy.scrapper.ScrapperConstants.BOT_UPDATES_URI;

import backend.academy.scrapper.dto.LinkUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

public class BotClient {
    private final RestClient restClient;
    private final Logger log = LoggerFactory.getLogger(BotClient.class);

    public BotClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void sendUpdateNotification(LinkUpdate linkUpdate) {
        try {
            restClient.post().uri(BOT_UPDATES_URI).body(linkUpdate).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Chat Id: {}{}", linkUpdate.getTgChatIds(), e.getMessage());
        }
    }
}
