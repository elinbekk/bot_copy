package backend.academy.scrapper;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.config.GithubProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static backend.academy.bot.entity.LinkType.GITHUB_REPO;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class GithubClientTest extends WiremockIntegrationTest {
    private GithubClient client;
    private TrackedResource trackedResource;
    private HttpClient mockHttpClient;

    @BeforeEach
    public void setUp() {
        GithubProperties githubProperties = new GithubProperties("dummy-token", "https://api.github.com");
        mockHttpClient = Mockito.mock(HttpClient.class);
        ObjectMapper objectMapper = new ObjectMapper();
        client = new GithubClient(githubProperties, mockHttpClient, objectMapper);
        trackedResource = createDefaultResource();
    }

    private TrackedResource createDefaultResource() {
        TrackedResource resource = new TrackedResource();
        resource.setLink("http://github.com/owner/repo");
        resource.setResourceType(GITHUB_REPO);
        resource.setLastCheckedTime(Instant.parse("2023-01-01T00:00:00Z"));
        return resource;
    }

    private void stubRepoRequest(ResponseDefinitionBuilder response) {
        wireMock.stubFor(get(urlEqualTo("/repos/owner/repo"))
            .withHeader("Accept", equalTo("application/vnd.github.v3+json"))
            .withHeader("Authorization", equalTo("token test-token"))
            .willReturn(response));
    }

    @Test
    public void hasUpdatesTest() throws Exception {
        String responseBody = """
            {
                "pushed_at": "2025-04-16T12:00:00Z"
            }
            """;
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponse);

        boolean result = client.hasUpdates(trackedResource);
        Assertions.assertTrue(result);
    }

    @Test
    void http404ErrorTest() {
        stubRepoRequest(notFound());

        boolean result = client.hasUpdates(trackedResource);
        Assertions.assertFalse(result);
    }

    @Test
    void missingPushedAtFieldTest() {
        stubRepoRequest(okJson("{\"name\":\"repo\"}"));

        boolean result = client.hasUpdates(trackedResource);
        Assertions.assertFalse(result);
    }

    @Test
    void http500ErrorTest() {
        stubRepoRequest(serverError());

        boolean result = client.hasUpdates(trackedResource);
        Assertions.assertFalse(result);
    }

    @Test
    void requestTimeoutTest() {
        stubRepoRequest(ok().withFixedDelay(15000));

        boolean result = client.hasUpdates(trackedResource);
        Assertions.assertFalse(result);
    }
}
