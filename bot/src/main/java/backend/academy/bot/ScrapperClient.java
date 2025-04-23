package backend.academy.bot;

import backend.academy.bot.dto.LinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.exception.DuplicateLinkException;
import backend.academy.bot.exception.LinkNotFoundException;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import static backend.academy.bot.constant.BotMessages.LINK_DUPLICATED_MESSAGE;

@Component
public class ScrapperClient {
    private final RestClient restClient;
    private static final String TG_CHAT_ENDPOINT = "/tg-chat/";
    private static final String LINKS_ENDPOINT = "/links";
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    public ScrapperClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void registerChat(Long chatId) {
        restClient.post()
            .uri(TG_CHAT_ENDPOINT + chatId)
            .retrieve()
            .toBodilessEntity();
    }

    public List<LinkResponse> getListLinks(Long chatId) {
        return restClient.get()
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, chatId.toString())
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });
    }

    public LinkResponse addLink(Long chatId, LinkRequest request) {
        return restClient.post()
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, chatId.toString())
            .body(request)
            .retrieve()
            .onStatus(
                HttpStatus.CONFLICT::equals,
                (req, res) -> {
                    throw new DuplicateLinkException(LINK_DUPLICATED_MESSAGE);
                }
            )
            .body(LinkResponse.class);
    }

    public void removeLink(Long chatId, LinkRequest request) {
        restClient.method(HttpMethod.DELETE)
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, chatId.toString())
            .body(request)
            .retrieve()
            .onStatus(
                HttpStatus.NOT_FOUND::equals,
                (req, res) -> {
                    throw new LinkNotFoundException(res.getStatusText());
                }
            )
            .toBodilessEntity();
    }
}
