package backend.academy.scrapper.client;

import backend.academy.bot.entity.TrackedResource;
import backend.academy.scrapper.config.StackoverflowProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class StackOverflowClient implements UpdateChecker {
    private static final Logger log = LoggerFactory.getLogger(StackOverflowClient.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final StackoverflowProperties stackoverflowProperties;

    public StackOverflowClient(HttpClient httpClient,
                               ObjectMapper objectMapper,
                               StackoverflowProperties stackoverflowProperties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.stackoverflowProperties = stackoverflowProperties;
    }

    @Override
    public boolean hasUpdates(TrackedResource resource) {
        log.debug("Начало проверки обновлений для: {}", resource.getLink());
        try {
            JsonNode question = getQuestionById(resource);
            return isUpdated(question, resource.getLastCheckedTime());
        } catch (Exception e) {
            throw new RuntimeException("Failed to check updates for: " + resource.getLink(), e);
        }
    }

    //todo: вообще, мы можем из ответа stackoverflow согласно их API сами создать класс ответа
    // (какой-нибудь StackOverflowQuestion),
    // чтобы не возвращать JsonNode, а уже классный объект
    private JsonNode getQuestionById(TrackedResource resource) throws Exception {
        URI uri = buildUrlWithFilters(resource);
        log.debug("Сформированный URL: {}", uri);
        HttpRequest request = buildRequest(String.valueOf(uri));

        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );
        log.debug("Статус ответа: {}", response.statusCode());
        log.trace("Тело ответа: {}", response.body());

        checkForErrors(response);
        return parseResponse(response.body());
    }


    private URI buildUrlWithFilters(TrackedResource resource) {
        int questionId = extractQuestionId(resource.getLink());

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(stackoverflowProperties.apiUrl())
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


    //todo: можно для таких манипулаций с урлами попробовать использовать UriBuilder, чтобы не заниматься этим самому
    private String buildTagsParam(Set<String> tags) {
        return tags.isEmpty() ? ""
            : "&tagged=" + String.join(";", tags);
    }

    private String buildFiltersParam(Map<String, String> filters) {
        if (filters.isEmpty()) {
            return "";
        } else {
            return filters.entrySet().stream()
                .map(e -> "&" + e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining());
        }
    }

    private String buildAuthParams() {
        return String.format("&key=%s&access_token=%s", stackoverflowProperties.key(), stackoverflowProperties.accessToken());
    }

    public boolean isUpdated(JsonNode question, Instant lastChecked) {
        long lastActivity = question.get("last_activity_date").asLong();
        log.debug("Дата обновления из API: {}", lastActivity);
        return lastActivity > lastChecked.getEpochSecond();
    }

    public JsonNode getQuestionById(int questionId) {
        String url = buildUrl(questionId);
        HttpRequest request = buildRequest(url);

        try {
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            checkForErrors(response);
            return parseResponse(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("API request failed", e);
        }
    }

    public int extractQuestionId(String url) {
        Pattern pattern = Pattern.compile("https?://stackoverflow.com/questions/(\\d+)");
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid StackOverflow URL: " + url);
        }
        return Integer.parseInt(matcher.group(1));
    }


    public String buildUrl(int questionId) {
        return String.format(
            "%squestions/%d?site=stackoverflow&filter=withbody&key=%s&access_token=%s",
            stackoverflowProperties.apiUrl(),
            questionId,
            stackoverflowProperties.key(),
            stackoverflowProperties.accessToken()
        );
    }

    private HttpRequest buildRequest(String url) {
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("Accept", "application/json")
            .GET()
            .build();
    }

    private void checkForErrors(HttpResponse<String> response) {
        int statusCode = response.statusCode();

        if (statusCode >= 400 && statusCode < 500) {
            throw new RuntimeException("Client error: " + response.body());
        } else if (statusCode >= 500) {
            throw new RuntimeException("Server error: " + response.body());
        }
    }

    public JsonNode parseResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode items = root.get("items");

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("No question found");
        }

        //todo:корректно ли только первый item брать?
        // остальные нужно обрабатывать? (если нет, то опиши почему достаточно только один обрабатывать)
        return items.get(0);
    }

}
