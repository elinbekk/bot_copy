package backend.academy.scrapper;

import backend.academy.bot.entity.TrackedResource;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.config.StackoverflowProperties;
import backend.academy.scrapper.exception.StackOverflowException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertThrows;

public class StackOverflowClientTest extends WiremockIntegrationTest {
    private StackOverflowClient client;
    private TrackedResource resource;
    private final String jsonDataWithOneItem = """
        {
            "items": [
                {
                    "last_activity_date": 1672531222,
                    "title": "Test Question"
                }
            ]
        }""";


    @BeforeEach
    void setup() {
        String baseUrl = wireMock.baseUrl() + "/";
        StackoverflowProperties soProperties = new StackoverflowProperties("test-key", "test-token", baseUrl);

        client = new StackOverflowClient(HttpClient.newHttpClient(), new ObjectMapper(), soProperties);

        resource = new TrackedResource();
        resource.setLink("https://stackoverflow.com/questions/12345/some-title");
        resource.setTags(Set.of("java"));
        resource.setFilters(Map.of("sort", "votes"));
        resource.setLastCheckedTime(Instant.parse("2023-01-01T00:00:00Z"));
    }


    @Test
    void extractQuestionIdByValidUrlTest() {
        int id = client.extractQuestionId("https://stackoverflow.com/questions/12345/some-title");
        Assertions.assertEquals(12345, id);
    }

    @Test
    void buildedUrlContainsAuthParamsTest() {
        String url = client.buildUrl(123);
        Assertions.assertTrue(url.contains("key=test-key"));
        Assertions.assertTrue(url.contains("access_token=test-token"));
    }

    @Test
    void extractQuestionId_InvalidUrl_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> client.extractQuestionId("https://stackoverflow.com/users/12345"));
    }

    @Test
    void parseResponseItemTest() {
        String jsonData = """
            {
                "items": [
                    {
                        "last_activity_date": 1672531222,
                        "title": "Test1"
                    },
                    {
                        "last_activity_date": 1672531222,
                        "title": "Test2"
                    }
                ]
            }""";
        StackOverflowQuestion result = client.parseResponse(jsonData);
        Assertions.assertEquals("Test1", result.getTitle());
    }


    @Test
    void isUpdated_NewActivity_ReturnsTrue() {
        StackOverflowQuestion question = client.parseResponse(jsonDataWithOneItem);
        boolean result = client.isUpdated(question, Instant.parse("2023-01-01T00:00:00Z"));
        Assertions.assertTrue(result);
    }

    @Test
    void isUpdated_OldActivity_ReturnsFalse() {
        StackOverflowQuestion question = client.parseResponse(jsonDataWithOneItem);
        boolean result = client.isUpdated(question, Instant.parse("2024-01-01T00:00:00Z"));
        Assertions.assertFalse(result);
    }

    @Test
    void getQuestion_TimeoutThrowsExceptionTest() {
        wireMock.stubFor(get(anyUrl())
            .willReturn(ok().withFixedDelay(20000)));
        StackOverflowException ex = assertThrows(
            StackOverflowException.class,
            () -> client.hasUpdates(resource)
        );
        Assertions.assertTrue(ex.getMessage().contains("Не удалось выполнить HTTP‑запрос к"));
    }

    @Test
    void clientError4xxTest() {
        int questionId = client.extractQuestionId(resource.getLink());
        wireMock.stubFor(get(urlPathEqualTo("/2.3/questions/" + questionId))
            .withQueryParam("key", equalTo("test-key"))
            .withQueryParam("access_token", equalTo("test-token"))
            .willReturn(aResponse()
                .withStatus(404)
                .withBody("Not Found")));

        StackOverflowException ex = assertThrows(
            StackOverflowException.class,
            () -> client.hasUpdates(resource)
        );
        Assertions.assertTrue(ex.getMessage().contains("Ошибка клиента: Not Found"));
    }

    @Test
    void serverError5xxTest() {
        int questionId = client.extractQuestionId(resource.getLink());
        wireMock.stubFor(get(urlPathEqualTo("/2.3/questions/" + questionId))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Oops")));

        StackOverflowException ex = assertThrows(
            StackOverflowException.class,
            () -> client.hasUpdates(resource)
        );
        Assertions.assertTrue(ex.getMessage().contains("Ошибка сервера"));
    }

    @Test
    void badJsonParsingTest() {
        int questionId = client.extractQuestionId(resource.getLink());
        wireMock.stubFor(get(urlPathEqualTo("/2.3/questions/" + questionId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("not a json")));

        StackOverflowException ex = assertThrows(
            StackOverflowException.class,
            () -> client.hasUpdates(resource)
        );
        Assertions.assertTrue(ex.getMessage().contains("Не удалось распарсить JSON‑ответ"));
    }

    @Test
    void noItemsThrowsTest() {
        String body = "{ \"items\": [] }";
        int questionId = client.extractQuestionId(resource.getLink());
        wireMock.stubFor(get(urlPathEqualTo("/2.3/questions/" + questionId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(body)));

        StackOverflowException ex = assertThrows(
            StackOverflowException.class,
            () -> client.hasUpdates(resource)
        );
        Assertions.assertTrue(ex.getMessage().contains("В ответе нет ни одного вопроса"));
    }
}
