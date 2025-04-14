package backend.academy.scrapper.client;

import org.springframework.web.client.RestClient;

public class BotClient {
    private final RestClient restClient;

    public BotClient(RestClient restClient) {
        this.restClient = restClient;
    }
}
