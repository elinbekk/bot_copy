package backend.academy.bot;

import static backend.academy.bot.constant.BotMessages.LINK_DUPLICATED_MESSAGE;

import backend.academy.bot.dto.LinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.exception.DuplicateLinkException;
import backend.academy.bot.exception.LinkNotFoundException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Component
public class ScrapperClient {
    private static final Logger logger = LoggerFactory.getLogger(ScrapperClient.class);
    private final RestClient restClient;
    private static final String TG_CHAT_ENDPOINT = "/tg-chat/";
    private static final String LINKS_ENDPOINT = "/links";
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    public ScrapperClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void registerChat(Long chatId) {
        logger.info("Регистрация чата: {}", chatId);
        try {
            restClient.post().uri(TG_CHAT_ENDPOINT + chatId).retrieve().toBodilessEntity();
            logger.info("Чат {} успешно зарегистрирован", chatId);
        } catch (HttpStatusCodeException e) {
            logger.error("Ошибка в регистрации чата {}: {}", chatId, e.getMessage());
            throw e;
        }
    }

    public List<LinkResponse> getListLinks(Long chatId) {
        return restClient
                .get()
                .uri(LINKS_ENDPOINT)
                .header(TG_CHAT_ID_HEADER, chatId.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public LinkResponse addLink(Long chatId, LinkRequest request) {
        logger.info("Добавление в чат {}: ссылка {}", chatId, request.getLink());
        LinkResponse response = restClient
                .post()
                .uri(LINKS_ENDPOINT)
                .header(TG_CHAT_ID_HEADER, chatId.toString())
                .body(request)
                .retrieve()
                .onStatus(HttpStatus.CONFLICT::equals, (req, res) -> {
                    logger.warn("Попытка добавления дубликата: {} для чата {}", request.getLink(), chatId);
                    throw new DuplicateLinkException(LINK_DUPLICATED_MESSAGE);
                })
                .body(LinkResponse.class);
        logger.info("Ссылка успешно {} добавлена в чат {}", request.getLink(), chatId);
        return response;
    }

    public void removeLink(Long chatId, LinkRequest request) {
        logger.info("Удаление ссылки для {}: {}", chatId, request.getLink());
        try {
            restClient
                    .method(HttpMethod.DELETE)
                    .uri(LINKS_ENDPOINT)
                    .header(TG_CHAT_ID_HEADER, chatId.toString())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatus.NOT_FOUND::equals, (req, res) -> {
                        throw new LinkNotFoundException(res.getStatusText());
                    })
                    .toBodilessEntity();
            logger.info("Ссылка успешно {} удалена для чата {}", request.getLink(), chatId);
        } catch (LinkNotFoundException e) {
            logger.warn("Ссылка для удаления не найдена {} в чате {}", request.getLink(), chatId);
        }
    }
}
