package backend.academy.scrapper;

import backend.academy.bot.entity.TrackedResource;
import backend.academy.scrapper.client.GithubClient;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

class GithubClientTest extends WiremockIntegrationTest {
    private GithubClient client;
    private TrackedResource resource;

    @BeforeEach
    void setUp() {
        client = new GithubClient("token");
        resource = createDefaultResource();
    }

    private TrackedResource createDefaultResource() {
        TrackedResource resource = new TrackedResource();
        resource.setLink("http://github.com/owner/repo");
        resource.setLastCheckedTime(Instant.parse("2023-01-01T00:00:00Z"));
        return resource;
    }

    private void stubRepoRequest(ResponseDefinitionBuilder response) {
        wireMock.stubFor(get(urlEqualTo("/repos/owner/repo"))
            .willReturn(response));
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
