package backend.academy.scrapper;

import static backend.academy.scrapper.entity.LinkType.GITHUB_REPO;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.config.GithubProperties;
import backend.academy.scrapper.entity.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GithubClientTest extends WiremockIntegrationTest {
    private GithubClient client;
    private Link link;

    @BeforeEach
    public void setUp() {
        String baseUrl = wireMock.baseUrl();
        GithubProperties githubProperties = new GithubProperties("dummy-token", baseUrl);
        ObjectMapper objectMapper = new ObjectMapper();
        client = new GithubClient(githubProperties, HttpClient.newHttpClient(), objectMapper);
        link = createDefaultResource();
    }

    private Link createDefaultResource() {
        Link resource = new Link();
        resource.setUrl("https://github.com/owner/repo");
        resource.setLinkType(GITHUB_REPO);
        resource.setLastCheckedTime("2023-01-01T00:00:00Z");
        return resource;
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    public void hasUpdatesTest(String apiResponseDate, boolean expectedResult) {
        wireMock.stubFor(get(urlPathEqualTo("/repos/owner/repo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("{\"pushed_at\":\"%s\"}", apiResponseDate))));

        boolean actualResult = client.hasUpdates(link);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(Arguments.of("2024-01-01T00:00:00Z", true), Arguments.of("2022-12-31T23:59:59Z", false));
    }

    @Test
    void http404ErrorTest() {
        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(404)));
        boolean result = client.hasUpdates(link);
        Assertions.assertFalse(result);
    }

    @Test
    void missingPushedAtFieldTest() {
        stubFor(get(urlPathEqualTo("/repos/owner/repo"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"name\":\"repo\"}")));
        boolean result = client.hasUpdates(link);
        Assertions.assertFalse(result);
    }

    @Test
    void http500ErrorTest() {
        stubFor(get(urlPathEqualTo("/repos/owner/repo")).willReturn(aResponse().withStatus(500)));

        Assertions.assertFalse(client.hasUpdates(link));
    }

    @Test
    void requestTimeoutTest() {
        stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withFixedDelay(5000) // Задержка 5 сек
                        .withStatus(200)));

        boolean result = client.hasUpdates(link);
        Assertions.assertFalse(result);
    }
}
