package backend.academy.scrapper.client;

import backend.academy.scrapper.config.GithubProperties;
import backend.academy.scrapper.dto.GithubIssue;
import backend.academy.scrapper.dto.GithubPR;
import backend.academy.scrapper.dto.GithubRepo;
import backend.academy.scrapper.dto.GithubResource;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GithubClient implements UpdateChecker {
    private static final Logger log = LoggerFactory.getLogger(GithubClient.class);

    private final GithubProperties githubProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GithubClient(GithubProperties githubProperties, HttpClient httpClient, ObjectMapper objectMapper) {
        this.githubProperties = githubProperties;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean hasUpdates(Link link) {
        try {
            log.info("Проверка обновлений: link={}", link.getUrl());

            URI uri = buildUriWithFilters(link);
            log.info("URI запроса: {}", uri);

            HttpRequest request = buildRequest(uri);
            HttpResponse<String> response = sendRequest(request);
            log.info("Status Code: {}", response.statusCode());

            JsonNode json = objectMapper.readTree(response.body());
            Instant lastUpdate = parseUpdateTime(json, link.getLinkType());

            return lastUpdate.isAfter(Instant.parse(link.getLastCheckedTime()));
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private Instant parseUpdateTime(JsonNode json, LinkType linkType) {
        String dateString =
                switch (linkType) {
                    case GITHUB_REPO -> json.get("pushed_at").asText();
                    case GITHUB_ISSUE, GITHUB_PR -> json.get("updated_at").asText();
                    default -> throw new IllegalArgumentException("Неподдерживаемый тип ссылки");
                };

        log.info("Дата обновления из API: {}", dateString);
        return Instant.parse(dateString);
    }

    private URI buildUriWithFilters(Link resource) {
        GithubResource gitHubResource = parseGitHubUrl(resource.getUrl());
        String basePath = buildBaseApiPath(gitHubResource);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(githubProperties.apiUrl() + basePath);

        if (resource.getFilters() != null) {
            resource.getFilters().forEach(builder::queryParam);
        }
        return builder.build().toUri();
    }

    private String buildBaseApiPath(GithubResource resource) {
        switch (resource.getType()) {
            case GITHUB_REPO -> {
                GithubRepo repo = (GithubRepo) resource;
                return repo.getApiPath();
            }
            case GITHUB_ISSUE -> {
                GithubIssue issue = (GithubIssue) resource;
                return issue.getApiPath();
            }
            case GITHUB_PR -> {
                GithubPR pr = (GithubPR) resource;
                return pr.getApiPath();
            }
            case STACKOVERFLOW -> {
                return null;
            }
        }
        return "";
    }

    private GithubResource parseGitHubUrl(String url) {
        final String repoRegex = "https?://github.com/([^/]+)/([^/]+)/?";
        final String issueRegex = "https?://github.com/([^/]+)/([^/]+)/issues/(\\d+)";
        final String prRegex = "https?://github.com/([^/]+)/([^/]+)/pull/(\\d+)";

        Pattern repoPattern = Pattern.compile(repoRegex);
        Pattern issuePattern = Pattern.compile(issueRegex);
        Pattern prPattern = Pattern.compile(prRegex);

        Matcher issueMatcher = issuePattern.matcher(url);
        Matcher prMatcher = prPattern.matcher(url);
        Matcher repoMatcher = repoPattern.matcher(url);

        if (issueMatcher.find()) {
            return new GithubIssue(
                issueMatcher.group(1), issueMatcher.group(2), Integer.parseInt(issueMatcher.group(3)));
        } else if (prMatcher.find()) {
            return new GithubPR(prMatcher.group(1), prMatcher.group(2), Integer.parseInt(prMatcher.group(3)));
        } else if (repoMatcher.find()) {
            return new GithubRepo(repoMatcher.group(1), repoMatcher.group(2));
        }
        throw new IllegalArgumentException("Неподдерживаемый Github URL");
    }

    private HttpRequest buildRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "token " + githubProperties.token())
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
