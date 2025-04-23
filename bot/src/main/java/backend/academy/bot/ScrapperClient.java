package backend.academy.bot;

import static backend.academy.bot.constant.BotMessages.LINK_DUPLICATED_MESSAGE;

import backend.academy.bot.dto.LinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.exception.DuplicateLinkException;
import backend.academy.bot.exception.LinkNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Component
public class ScrapperClient {
//    private final RestClient restClient;
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private static final String TG_CHAT_ENDPOINT = "/tg-chat/";
    private static final String LINKS_ENDPOINT = "/links";
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    public ScrapperClient(RestTemplate restTemplate, @Value("${app.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void registerChat(Long chatId) {
        String url = baseUrl + TG_CHAT_ENDPOINT + chatId;
        restTemplate.postForLocation(url, null);
    }

    public List<LinkResponse> getListLinks(Long chatId) {
        String url = baseUrl + LINKS_ENDPOINT;
        HttpHeaders headers = new HttpHeaders();
        headers.set(TG_CHAT_ID_HEADER, String.valueOf(chatId));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<List<LinkResponse>> response =
                restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public LinkResponse addLink(Long chatId, LinkRequest request) {
        String url = baseUrl + LINKS_ENDPOINT;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TG_CHAT_ID_HEADER, String.valueOf(chatId));
        HttpEntity<LinkRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<LinkResponse> response = restTemplate.postForEntity(url, requestEntity, LinkResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException.Conflict e) {
            throw new DuplicateLinkException(LINK_DUPLICATED_MESSAGE);
        }
    }

    public void removeLink(Long chatId, LinkRequest request) {
        String url = baseUrl + LINKS_ENDPOINT;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TG_CHAT_ID_HEADER, String.valueOf(chatId));
        HttpEntity<LinkRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
        } catch (HttpClientErrorException e) {
            throw new LinkNotFoundException(e.getMessage());
        }
    }
}
