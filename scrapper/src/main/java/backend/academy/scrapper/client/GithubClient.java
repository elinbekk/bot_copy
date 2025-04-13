package backend.academy.scrapper.client;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.entity.TrackedResource;
import backend.academy.scrapper.GithubResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import static backend.academy.bot.entity.LinkType.GITHUB_ISSUE;
import static backend.academy.bot.entity.LinkType.GITHUB_PR;
import static backend.academy.bot.entity.LinkType.GITHUB_REPO;

@Component
public class GithubClient implements UpdateChecker {
    private static final Logger log = LoggerFactory.getLogger(GithubClient.class);
    private final HttpClient httpClient;
    private final String apiToken;

    public GithubClient(@Value("${github.token}") String apiToken) {
        this.httpClient = HttpClient.newHttpClient();
        this.apiToken = apiToken;
    }

    @Override
    public boolean hasUpdates(TrackedResource resource) {
        try {
            log.debug("Начало проверки обновлений для: {}", resource.getLink());
            URI uri = buildUriWithFilters(resource);
            log.debug("Сформированный URI: {}", uri);

            HttpRequest request = buildRequest(uri);
            HttpResponse<String> response = sendRequest(request);

            log.debug("Статус ответа: {}", response.statusCode());
            log.trace("Тело ответа: {}", response.body());

            JsonNode json = new ObjectMapper().readTree(response.body());
            Instant lastUpdate = parseUpdateTime(json, resource.getResourceType());

            return lastUpdate.isAfter(resource.getLastCheckedTime());
        } catch (Exception e) {
            log.error("Ошибка при проверке обновлений: ", e);
            return false;
        }
    }

    private Instant parseUpdateTime(JsonNode json, LinkType linkType) {
        String dateString = switch (linkType) {
            case GITHUB_REPO -> json.get("pushed_at").asText();
            case GITHUB_ISSUE, GITHUB_PR -> json.get("updated_at").asText();
            default -> throw new IllegalArgumentException("Неподдерживаемый тип ссылки");
        };

        log.debug("Дата обновления из API: {}", dateString);
        return Instant.parse(dateString);
    }

    private URI buildUriWithFilters(TrackedResource resource) throws URISyntaxException {
        GithubResource gitHubResource = parseGitHubUrl(resource.getLink());
        String basePath = buildBaseApiPath(gitHubResource);

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString("https://api.github.com" + basePath);

        if (resource.getFilters() != null) {
            resource.getFilters().forEach(builder::queryParam);
        }
        log.info("Final URI: {}", builder.build().toUriString());
        return builder.build().toUri();
    }

    private String buildBaseApiPath(GithubResource resource) {
        return switch (resource.getType()) {
            case GITHUB_REPO -> "/repos/%s/%s".formatted(resource.getOwner(), resource.getRepo());
            case GITHUB_ISSUE -> "/repos/%s/%s/issues/%s".formatted(
                resource.getOwner(),
                resource.getRepo(),
                resource.getNumber()
            );
            case GITHUB_PR -> "/repos/%s/%s/pulls/%s".formatted(
                resource.getOwner(),
                resource.getRepo(),
                resource.getNumber()
            );
            case STACKOVERFLOW -> null;
        };
    }

    private GithubResource parseGitHubUrl(String url) throws URISyntaxException {
        Pattern repoPattern = Pattern.compile("https?://github.com/([^/]+)/([^/]+)/?");
        Pattern issuePattern = Pattern.compile("https?://github.com/([^/]+)/([^/]+)/issues/(\\d+)");
        Pattern prPattern = Pattern.compile("https?://github.com/([^/]+)/([^/]+)/pull/(\\d+)");

        Matcher issueMatcher = issuePattern.matcher(url);
        Matcher prMatcher = prPattern.matcher(url);
        Matcher repoMatcher = repoPattern.matcher(url);

        if (issueMatcher.find()) {
            return new GithubResource(
                GITHUB_ISSUE,
                issueMatcher.group(1),
                issueMatcher.group(2),
                issueMatcher.group(3)
            );
        } else if (prMatcher.find()) {
            return new GithubResource(
                GITHUB_PR,
                prMatcher.group(1),
                prMatcher.group(2),
                prMatcher.group(3)
            );
        } else if (repoMatcher.find()) {
            return new GithubResource(
                GITHUB_REPO,
                repoMatcher.group(1),
                repoMatcher.group(2),
                null
            );
        }
        throw new IllegalArgumentException("Неподдерживаемый Github URL");
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
}
