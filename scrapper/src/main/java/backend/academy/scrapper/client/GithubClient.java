package backend.academy.scrapper.client;

import static backend.academy.scrapper.ScrapperConstants.ISSUE_REGEX;
import static backend.academy.scrapper.ScrapperConstants.PR_REGEX;
import static backend.academy.scrapper.ScrapperConstants.REPO_REGEX;

import backend.academy.scrapper.config.GithubProperties;
import backend.academy.scrapper.dto.GithubIssue;
import backend.academy.scrapper.dto.GithubPR;
import backend.academy.scrapper.dto.GithubRepo;
import backend.academy.scrapper.dto.GithubResource;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GithubClient implements UpdateChecker {
    private static final Logger log = LoggerFactory.getLogger(GithubClient.class);

    private final GithubProperties githubProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GithubClient(GithubProperties githubProperties, RestClient restClient, ObjectMapper objectMapper) {
        this.githubProperties = githubProperties;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean hasUpdates(Link link) {
        try {
            log.info("Проверка обновлений: link={}", link.getUrl());

            URI uri = getUri(link);
            log.info("URI запроса: {}", uri);

            final ResponseEntity<String> response = getResponse(uri);

            JsonNode json = objectMapper.readTree(response.getBody());
            Instant lastUpdate = parseUpdateTime(json, link.getLinkType());
            log.info("Status Code: {}", response.getStatusCode());

            return lastUpdate.isAfter(Instant.parse(link.getLastCheckedTime()));
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Ошибка при запросе: {}", e.getMessage());
            return false;
        } catch (JsonProcessingException e) {
            log.error("Ошибка парсинга JSON: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Ошибка:{}", e.getMessage());
            return false;
        }
    }

    private ResponseEntity<String> getResponse(URI uri) {
        ResponseEntity<String> response = restClient
                .get()
                .uri(uri)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "token " + githubProperties.token())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.error("Клиентская ошибка: {} {}", res.getStatusCode(), res.getStatusText());
                    throw new HttpClientErrorException(res.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("Серверная ошибка: {}", res.getStatusCode());
                    throw new HttpServerErrorException(res.getStatusCode());
                })
                .toEntity(String.class);
        return response;
    }

    private URI getUri(Link link) {
        return buildUriWithFilters(link);
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
        Pattern repoPattern = Pattern.compile(REPO_REGEX);
        Pattern issuePattern = Pattern.compile(ISSUE_REGEX);
        Pattern prPattern = Pattern.compile(PR_REGEX);

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
}
