package backend.academy.scrapper;

import backend.academy.bot.entity.TrackedResource;
import backend.academy.scrapper.client.GithubClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import java.time.Instant;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@WireMockTest
class GithubClientTest {
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @Test
    void testHttp404Error() {
        GithubClient client = new GithubClient("token");

        wireMock.stubFor(get(urlEqualTo("/repos/owner/repo"))
            .willReturn(notFound()));

        TrackedResource resource = new TrackedResource();
        resource.setLink("http://github.com/owner/repo");
        resource.setLastCheckedTime(Instant.parse("2023-01-01T00:00:00Z"));
        boolean result = client.hasUpdates(resource);
        Assertions.assertFalse(result);
    }

    @Test
    void testMissingPushedAtField() {
        GithubClient client = new GithubClient("token");

        wireMock.stubFor(get(urlEqualTo("/repos/owner/repo"))
            .willReturn(okJson("{\"name\":\"repo\"}")));

        TrackedResource resource = new TrackedResource();
        resource.setLink("http://github.com/owner/repo");
        resource.setLastCheckedTime(Instant.parse("2023-01-01T00:00:00Z"));
        boolean result = client.hasUpdates(resource);

        Assertions.assertFalse(result);
    }

    @Test
    void testHttp500Error() {
        GithubClient client = new GithubClient("token");

        wireMock.stubFor(get(urlEqualTo("/repos/owner/repo"))
            .willReturn(serverError()));

        boolean result = client.hasUpdates("http://github.com/owner/repo", Instant.parse("2023-01-01T00:00:00Z"));
        Assertions.assertFalse(result);
    }

    @Test
    void testRequestTimeout() {
        GithubClient client = new GithubClient("token");

        wireMock.stubFor(get(urlEqualTo("/repos/owner/repo"))
            .willReturn(ok().withFixedDelay(15000))); // 15 секунд > 10

        boolean result = client.hasUpdates("http://github.com/owner/repo", Instant.parse("2023-01-01T00:00:00Z"));
        Assertions.assertFalse(result);
    }
}
