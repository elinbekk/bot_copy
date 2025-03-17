package backend.academy.scrapper.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GithubClient implements UpdateChecker {
    private final HttpClient httpClient;
    private final String apiToken;

    public GithubClient(@Value("${github.token}") String apiToken) {
        this.httpClient = HttpClient.newHttpClient();
        this.apiToken = apiToken;
    }

    @Override
    public boolean hasUpdates(String url, String lastChecked) {
        try {
            URI uri = parseGitHubUrl(url);
            HttpRequest request = buildRequest(uri);
            HttpResponse<String> response = sendRequest(request);

            return isUpdated(response.body(), lastChecked);
        } catch (Exception e) {
            return false;
        }
    }

    private URI parseGitHubUrl(String url) throws URISyntaxException {
        Pattern pattern = Pattern.compile("https?://github.com/([^/]+)/([^/]+)");
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid GitHub URL");
        }

        return new URI("https://api.github.com/repos/"
            + matcher.group(1) + "/" + matcher.group(2));
    }

    private HttpRequest buildRequest(URI uri) {
        return HttpRequest.newBuilder()
            .uri(uri)
            .header("Accept", "application/vnd.github.v3+json")
            .header("Authorization", "token " + apiToken)
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private boolean isUpdated(String responseBody, String lastChecked) throws JsonProcessingException {
        JsonNode json = new ObjectMapper().readTree(responseBody);
        String updatedAt = json.get("pushed_at").asText();
        return updatedAt.compareTo(lastChecked) > 0;
    }
}
