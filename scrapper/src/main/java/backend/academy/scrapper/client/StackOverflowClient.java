package backend.academy.scrapper.client;

import backend.academy.scrapper.config.StackoverflowProperties;
import backend.academy.scrapper.dto.StackOverflowQuestion;
import backend.academy.scrapper.dto.StackOverflowResponse;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.StackOverflowException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import static backend.academy.scrapper.ScrapperConstants.SO_REGEX;

@Component
public class StackOverflowClient implements UpdateChecker {
    private static final Logger log = LoggerFactory.getLogger(StackOverflowClient.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final StackoverflowProperties stackoverflowProperties;

    public StackOverflowClient(
            RestClient restClient, ObjectMapper objectMapper, StackoverflowProperties stackoverflowProperties) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.stackoverflowProperties = stackoverflowProperties;
    }

    @Override
    public boolean hasUpdates(Link resource) {
        log.debug("Начало проверки обновлений для: {}", resource.getUrl());
        try {
            StackOverflowQuestion question = getQuestion(resource);
            return isUpdated(question, Instant.parse(resource.getLastCheckedTime()));
        } catch (Exception e) {
            log.error("Ошибка проверки обновления для: {}", resource.getUrl(), e);
            throw new StackOverflowException(e.getMessage());
        }
    }

    private StackOverflowQuestion getQuestion(Link resource) {
        final StackOverflowResponse stackOverflowResponse = getStackOverflowResponse(resource);

        if (stackOverflowResponse == null
                || stackOverflowResponse.getItems() == null
                || stackOverflowResponse.getItems().isEmpty()) {
            throw new StackOverflowException("Пустой ответ от Stackoverflow");
        }
        return stackOverflowResponse.getItems().getFirst();
    }

    private @Nullable StackOverflowResponse getStackOverflowResponse(Link resource) {
        StackOverflowResponse stackOverflowResponse = restClient
                .get()
                .uri(buildUrlWithFilters(resource))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes());
                    throw new StackOverflowException("Клиентская ошибка: " + response.getStatusCode() + " " + body);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new StackOverflowException("Серверная ошибка: " + response.getStatusCode());
                })
                .body(StackOverflowResponse.class);
        return stackOverflowResponse;
    }

    private URI buildUrlWithFilters(Link resource) {
        int questionId = extractQuestionId(resource.getUrl());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(stackoverflowProperties.apiUrl())
                .path("/2.3/questions/{id}")
                .queryParam("site", "stackoverflow")
                .queryParam("filter", "withbody")
                .queryParam("key", stackoverflowProperties.key())
                .queryParam("access_token", stackoverflowProperties.accessToken());

        if (!resource.getTags().isEmpty()) {
            builder.queryParam("tagged", String.join(";", resource.getTags()));
        }

        if (!resource.getFilters().isEmpty()) {
            resource.getFilters().forEach(builder::queryParam);
        }
        return builder.build(questionId);
    }

    public boolean isUpdated(StackOverflowQuestion question, Instant lastChecked) {
        long lastActivity = question.getLastActivityDate();
        log.debug("Дата обновления из API: {}", lastActivity);
        return lastActivity > lastChecked.getEpochSecond();
    }

    public int extractQuestionId(String url) {
        Pattern pattern = Pattern.compile(SO_REGEX);
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Неправильный StackOverflow URL: " + url);
        }
        return Integer.parseInt(matcher.group(1));
    }

    public StackOverflowQuestion parseResponse(String body) {
        try {
            StackOverflowResponse apiResp = objectMapper.readValue(body, StackOverflowResponse.class);
            if (apiResp.getItems() == null || apiResp.getItems().isEmpty()) {
                throw new StackOverflowException("В ответе нет ни одного вопроса");
            }
            return apiResp.getItems().getFirst();
        } catch (JsonProcessingException e) {
            throw new StackOverflowException("Не удалось распарсить JSON‑ответ: " + e.getOriginalMessage(), e);
        }
    }

}
