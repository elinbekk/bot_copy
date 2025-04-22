package backend.academy.scrapper;

import static backend.academy.scrapper.entity.LinkType.GITHUB_REPO;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.config.GithubProperties;
import backend.academy.scrapper.entity.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GithubClientTest extends WiremockIntegrationTest {
    private GithubClient client;
    private Link link;
    private HttpClient mockHttpClient;

    @BeforeEach
    public void setUp() {
        GithubProperties githubProperties = new GithubProperties("dummy-token", "https://api.github.com");
        mockHttpClient = Mockito.mock(HttpClient.class);
        ObjectMapper objectMapper = new ObjectMapper();
        client = new GithubClient(githubProperties, mockHttpClient, objectMapper);
        link = createDefaultResource();
    }

    private Link createDefaultResource() {
        Link resource = new Link();
        resource.setUrl("http://github.com/owner/repo");
        resource.setLinkType(GITHUB_REPO);
        resource.setLastCheckedTime("2023-01-01T00:00:00Z");
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
        String responseBody =
                """
            {
                "pushed_at": "2025-04-16T12:00:00Z"
            }
            """;
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        boolean result = client.hasUpdates(link);
        Assertions.assertTrue(result);
    }

    @Test
    void http404ErrorTest() {
        stubRepoRequest(notFound());

        boolean result = client.hasUpdates(link);
        Assertions.assertFalse(result);
    }

    @Test
    void missingPushedAtFieldTest() {
        stubRepoRequest(okJson("{\"name\":\"repo\"}"));

        boolean result = client.hasUpdates(link);
        Assertions.assertFalse(result);
    }

    @Test
    void http500ErrorTest() {
        stubRepoRequest(serverError());

        boolean result = client.hasUpdates(link);
        Assertions.assertFalse(result);
    }

    @Test
    void requestTimeoutTest() {
        stubRepoRequest(ok().withFixedDelay(15000));

        boolean result = client.hasUpdates(link);
        Assertions.assertFalse(result);
    }
}
