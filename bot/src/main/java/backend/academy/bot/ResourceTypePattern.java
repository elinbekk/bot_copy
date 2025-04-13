package backend.academy.bot;

import java.util.regex.Pattern;

public enum ResourceTypePattern {

    GITHUB_ISSUE(
        "https?://github\\.com/[^/]+/[^/]+/issues/\\d+",
        "GitHub Issue"
    ),
    GITHUB_PR(
        "https?://github\\.com/[^/]+/[^/]+/pull/\\d+",
        "GitHub Pull Request"
    ),
    GITHUB_REPO(
        "https?://github\\.com/[^/]+/[^/]+(/)?(.git)?",
        "GitHub Repository"
    ),
    STACKOVERFLOW(
        "https?://stackoverflow\\.com/questions/\\d+",
        "StackOverflow Question"
    );

    private final Pattern pattern;
    private final String description;

    ResourceTypePattern(String regex, String description) {
        this.pattern = Pattern.compile(regex);
        this.description = description;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getDescription() {
        return description;
    }
}

