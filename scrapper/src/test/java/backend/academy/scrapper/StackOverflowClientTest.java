package backend.academy.scrapper;

import backend.academy.scrapper.client.StackOverflowClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import java.time.Instant;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@WireMockTest
public class StackOverflowClientTest {
    private StackOverflowClient client;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @BeforeEach
    void setup() {
        String baseUrl = wireMock.baseUrl() + "/2.3/";
        client = new StackOverflowClient("test-key", "test-token", baseUrl);
    }

    @Test
    void extractQuestionId_ValidUrl_ReturnsCorrectId() {
        int id = client.extractQuestionId("https://stackoverflow.com/questions/12345/some-title");
        assertEquals(12345, id);
    }

    @Test
    void extractQuestionId_InvalidUrl_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            client.extractQuestionId("https://stackoverflow.com/users/12345");
        });
    }

    @Test
    void getQuestion_Success_ReturnsJsonNode() {
        wireMock.stubFor(get(urlPathEqualTo("/2.3/questions/123"))
            .withQueryParam("key", equalTo("test-key"))
            .withQueryParam("access_token", equalTo("test-token"))
            .willReturn(okJson("{ \"items\": [{\"last_activity_date\": 1672531200}] }")));

        JsonNode result = client.getQuestion(123);
        Assertions.assertTrue(result.has("last_activity_date"));
    }

    @Test
    void getQuestion_Http404_ThrowsException() {
        wireMock.stubFor(get(anyUrl())
            .willReturn(notFound()));

        assertThrows(RuntimeException.class, () -> client.getQuestion(123));
    }

    @Test
    void parseResponse_ValidJson_ReturnsFirstItem() throws Exception {
        String json = "{ \"items\": [{\"title\": \"Test\"}, {\"title\": \"Test2\"}] }";
        JsonNode result = client.parseResponse(json);
        assertTrue(result.has("title"));
    }

    @Test
    void parseResponse_EmptyItems_ThrowsException() {
        assertThrows(RuntimeException.class, () -> {
            client.parseResponse("{ \"items\": [] }");
        });
    }

    @Test
    void isUpdated_NewActivity_ReturnsTrue() throws Exception {
        String jsonData = "{ \"items\": [{\"last_activity_date\": 1672531222}] }";
        JsonNode json = new ObjectMapper().readTree(jsonData);
        boolean result = client.isUpdated(json, Instant.parse("2023-01-01T00:00:00Z"));
        Assertions.assertTrue(result);
    }

    @Test
    void isUpdated_OldActivity_ReturnsFalse() throws Exception {
        String jsonData = "{ \"items\": [{\"last_activity_date\": 1672531200}] }";
        JsonNode json = new ObjectMapper().readTree(jsonData);
        boolean result = client.isUpdated(json, Instant.parse("2024-01-01T00:00:00Z"));
        Assertions.assertFalse(result);
    }

    @Test
    void getQuestion_Timeout_ThrowsException() {
        wireMock.stubFor(get(anyUrl())
            .willReturn(ok().withFixedDelay(20000)));

        assertThrows(RuntimeException.class, () -> client.getQuestion(123));
    }

    @Test
    void buildUrl_ContainsAuthParams() {
        String url = client.buildUrl(123);
        Assertions.assertTrue(url.contains("key=test-key"));
        Assertions.assertTrue(url.contains("access_token=test-token"));
    }
}
