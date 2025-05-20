package backend.academy.scrapper;

import static backend.academy.scrapper.TestUtils.loadFixture;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertThrows;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.config.StackoverflowProperties;
import backend.academy.scrapper.dto.StackOverflowQuestion;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.StackOverflowException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.client.RestClient;

public class StackOverflowClientTest extends WiremockIntegrationTest {
    private StackOverflowClient client;
    private Link resource;

    @BeforeEach
    void setup() {
        String baseUrl = wireMock.baseUrl() + "/";
        StackoverflowProperties soProperties = new StackoverflowProperties("test-key", "test-token", baseUrl);

        RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();
        client = new StackOverflowClient(restClient, new ObjectMapper(), soProperties);

        resource = new Link();
        resource.setUrl("https://stackoverflow.com/questions/12345/some-title");
        resource.setTags(Set.of("java"));
        resource.setFilters(Map.of("sort", "votes"));
        resource.setLastCheckedTime("2023-01-01T00:00:00Z");
    }

    @Test
    void extractQuestionIdByValidUrlTest() {
        int id = client.extractQuestionId("https://stackoverflow.com/questions/12345/some-title");
        Assertions.assertEquals(12345, id);
    }

    @Test
    void extractQuestionId_InvalidUrl_ThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> client.extractQuestionId("https://stackoverflow.com/users/12345"));
    }

    @Test
    void parseResponseItemTest() {
        String jsonData;
        try {
            jsonData = loadFixture("fixtures/so_multiple_items.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        StackOverflowQuestion result = client.parseResponse(jsonData);
        Assertions.assertEquals("Test1", result.getTitle());
    }

    @Test
    void isUpdated_NewActivity_ReturnsTrue() throws Exception {
        String jsonDataWithOneItem = loadFixture("fixtures/single_so_item.json");
        StackOverflowQuestion question = client.parseResponse(jsonDataWithOneItem);
        boolean result = client.isUpdated(question, Instant.parse("2023-01-01T00:00:00Z"));
        Assertions.assertTrue(result);
    }

    @Test
    void isUpdated_OldActivity_ReturnsFalse() throws Exception {
        String jsonDataWithOneItem = loadFixture("fixtures/single_so_item.json");
        StackOverflowQuestion question = client.parseResponse(jsonDataWithOneItem);
        boolean result = client.isUpdated(question, Instant.parse("2024-01-01T00:00:00Z"));
        Assertions.assertFalse(result);
    }

    @Test
    void getQuestion_TimeoutThrowsExceptionTest() {
        wireMock.stubFor(get(anyUrl()).willReturn(ok().withFixedDelay(20000)));
        assertThrows(StackOverflowException.class, () -> client.hasUpdates(resource));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void parameterizedExceptionTest(int statusCode, String expectedMessagePart) {
        int questionId = client.extractQuestionId(resource.getUrl());
        wireMock.stubFor(get(urlPathEqualTo("/2.3/questions/" + questionId))
                .withQueryParam("key", equalTo("test-key"))
                .withQueryParam("access_token", equalTo("test-token"))
                .willReturn(aResponse().withStatus(statusCode)));

        StackOverflowException ex = assertThrows(StackOverflowException.class, () -> client.hasUpdates(resource));
        Assertions.assertTrue(
                ex.getMessage().contains(expectedMessagePart),
                "Сообщение исключения должно содержать: " + expectedMessagePart);
    }

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
                Arguments.of(404, "Клиентская ошибка"),
                Arguments.of(500, "Серверная ошибка"),
                Arguments.of(200, "Пустой ответ от Stackoverflow"));
    }
}
