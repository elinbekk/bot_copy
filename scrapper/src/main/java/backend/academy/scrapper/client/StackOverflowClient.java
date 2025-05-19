package backend.academy.scrapper.client;

import static backend.academy.scrapper.ScrapperConstants.SO_REGEX;

import backend.academy.scrapper.config.StackoverflowProperties;
import backend.academy.scrapper.dto.Link;
import backend.academy.scrapper.dto.StackOverflowQuestion;
import backend.academy.scrapper.dto.StackOverflowResponse;
import backend.academy.scrapper.exception.StackOverflowException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

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

    private StackOverflowResponse getStackOverflowResponse(Link resource) {
        StackOverflowResponse stackOverflowResponse = restClient
                .get()
                .uri(buildUrlWithFilters(resource))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxError)
                .body(StackOverflowResponse.class);
        return stackOverflowResponse;
    }

    private void handle5xxError(HttpRequest request, ClientHttpResponse response) throws IOException {
        throw new StackOverflowException("Серверная ошибка: " + response.getStatusCode());
    }

    private void handle4xxError(HttpRequest httpRequest, ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), Charset.defaultCharset());
        throw new StackOverflowException("Клиентская ошибка: " + response.getStatusCode() + " " + body);
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

    public Optional<Detail> fetchDetail(Link link) {
        try {
            StackOverflowResponse response = getStackOverflowResponse(link);
            StackOverflowQuestion question = response.getItems().getFirst();
            Instant lastActivity = Instant.ofEpochSecond(question.getLastActivityDate());
            Instant lastChecked = Instant.parse(link.getLastCheckedTime());
            if (!lastActivity.isAfter(lastChecked)) {
                return Optional.empty();
            }
            final ObjectNode payload = buildPayload(question);

            return Optional.of(new Detail(lastActivity, payload));
        } catch (Exception e) {
            log.error("Ошибка fetchDetail для SO: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private ObjectNode buildPayload(StackOverflowQuestion question) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("questionTitle", question.getTitle());
        payload.put("user", question.getOwner().getDisplayName());
        payload.put("createdAt", question.getCreatingDate());

        String body = question.getBody();
        payload.put("preview", body.length() <= 200 ? body : body.substring(0, 200));
        return payload;
    }

    public static class Detail {
        private final Instant lastUpdate;
        private final JsonNode payload;

        public Detail(Instant lastUpdate, JsonNode payload) {
            this.lastUpdate = lastUpdate;
            this.payload = payload;
        }

        public Instant getLastUpdate() {
            return lastUpdate;
        }

        public JsonNode getPayload() {
            return payload;
        }
    }
}
