package backend.academy.scrapper.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class StackOverflowClient implements UpdateChecker {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String soTokenKey;
    private final String soAccessToken;

    public StackOverflowClient(
        @Value("${stackoverflow.key:}") String key,
        @Value("${stackoverflow.token:}") String token
    ) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
        this.soTokenKey = key;
        this.soAccessToken = token;
    }

    @Override
    public boolean hasUpdates(String url, String lastChecked) {
        try {
            int questionId = extractQuestionId(url);
            JsonNode question = getQuestion(questionId);
            return isUpdated(String.valueOf(question), lastChecked);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check updates for: " + url, e);
        }
    }

    private JsonNode getQuestion(int questionId) {
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

    private int extractQuestionId(String url) {
        Pattern pattern = Pattern.compile("https?://stackoverflow.com/questions/(\\d+)");
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid StackOverflow URL: " + url);
        }
        return Integer.parseInt(matcher.group(1));
    }


    private String buildUrl(int questionId) {
        return String.format(
            "https://api.stackexchange.com/2.3/questions/%d?site=stackoverflow&filter=withbody&key=%s&access_token=%s",
            questionId,
            soTokenKey,
            soAccessToken
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

    private JsonNode parseResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode items = root.get("items");

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("No question found");
        }

        return items.get(0);
    }

    private boolean isUpdated(String responseBody, String lastChecked) throws JsonProcessingException {
        JsonNode root = new ObjectMapper().readTree(responseBody);
        JsonNode item = root.get("items").get(0);
        long lastActivity = item.get("last_activity_date").asLong();

        // Конвертируем lastChecked в timestamp для сравнения
        Instant lastCheckedInstant = Instant.parse(lastChecked);
        return lastActivity > lastCheckedInstant.getEpochSecond();
    }
}
