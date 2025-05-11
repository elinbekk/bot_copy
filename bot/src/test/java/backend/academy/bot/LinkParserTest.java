package backend.academy.bot;

import static org.junit.Assert.assertThrows;

import backend.academy.bot.helper.LinkTypeDetector;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LinkParserTest {
    private final LinkTypeDetector resourceTypeDetector = new LinkTypeDetector();

    @ParameterizedTest
    @MethodSource("positiveTestCases")
    void linkTypeCorrectTest(String url, LinkType expectedType) {
        Assertions.assertEquals(expectedType, resourceTypeDetector.detectResourceType(url));
    }

    @ParameterizedTest
    @MethodSource("negativeTestCases")
    void linkTypeIncorrectTest(String url) {
        assertThrows(IllegalArgumentException.class, () -> resourceTypeDetector.detectResourceType(url));
    }

    private static Stream<Arguments> positiveTestCases() {
        return Stream.of(
                Arguments.of("https://github.com/user/repo", LinkType.GITHUB_REPO),
                Arguments.of("https://github.com/user/repo/issues/123", LinkType.GITHUB_ISSUE),
                Arguments.of("https://github.com/user/repo/pull/4", LinkType.GITHUB_PR),
                Arguments.of("https://stackoverflow.com/questions/123", LinkType.STACKOVERFLOW));
    }

    private static Stream<Arguments> negativeTestCases() {
        return Stream.of(
            Arguments.of("https://dckmdcdkdnvo"),
            Arguments.of("https://invalid"),
            Arguments.of("https://google.com", null, true));
    }
}
