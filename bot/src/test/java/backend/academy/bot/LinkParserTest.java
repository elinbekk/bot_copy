package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.Assert.assertThrows;

class LinkParserTest {
    private final LinkTypeDetector resourceTypeDetector = new LinkTypeDetector();

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void linkTypeCorrectDetectedTest(String url, LinkType expectedType, boolean expectException) {
        if (expectException) {
            assertThrows(IllegalArgumentException.class,
                () -> resourceTypeDetector.detectResourceType(url));
        } else {
            Assertions.assertEquals(expectedType, resourceTypeDetector.detectResourceType(url));
        }
    }

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
            Arguments.of(
                "https://github.com/user/repo",
                LinkType.GITHUB_REPO,
                false
            ),
            Arguments.of(
                "https://github.com/user/repo/issues/123",
                LinkType.GITHUB_ISSUE,
                false
            ),
            Arguments.of(
                "https://github.com/user/repo/pull/4",
                LinkType.GITHUB_PR,
                false
            ),
            Arguments.of(
                "https://stackoverflow.com/questions/123",
                LinkType.STACKOVERFLOW,
                false
            ),
            Arguments.of(
                "https://google.com",
                null,
                true
            )
        );
    }
}
