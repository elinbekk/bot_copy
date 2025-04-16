package backend.academy.scrapper;

import backend.academy.bot.entity.TrackedResource;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.config.GithubProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import java.net.http.HttpClient;
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

class GithubClientTest extends WiremockIntegrationTest {
    private GithubClient client;
    private TrackedResource resource;
    HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void setUp() {
        GithubProperties githubProperties = new GithubProperties("test-token", wireMock.baseUrl());
        client = new GithubClient( githubProperties,
            mockHttpClient,
            objectMapper
        );
        resource = createDefaultResource();
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
    void hasUpdatesWhenRepoUpdatedTest() {
        String responseJson = """
            {
                "id": 123,
                "pushed_at": "2023-01-02T00:00:00Z",
                "name": "repo"
            }""";
        stubRepoRequest(okJson(responseJson));

        boolean result = client.hasUpdates(resource);
        Assertions.assertTrue(result);
    }

    @Test
    void testHttp404Error() {
        stubRepoRequest(notFound());

        boolean result = client.hasUpdates(resource);
        Assertions.assertFalse(result);
    }

    @Test
    void testMissingPushedAtField() {
        stubRepoRequest(okJson("{\"name\":\"repo\"}"));

        boolean result = client.hasUpdates(resource);
        Assertions.assertFalse(result);
    }

    @Test
    void testHttp500Error() {
        stubRepoRequest(serverError());

        boolean result = client.hasUpdates(resource);
        Assertions.assertFalse(result);
    }

    @Test
    void testRequestTimeout() {
        stubRepoRequest(ok().withFixedDelay(15000));

        boolean result = client.hasUpdates(resource);
        Assertions.assertFalse(result);
    }
}
