package backend.academy.bot.entity;

import java.util.regex.Pattern;

public enum LinkType {
    GITHUB_REPO(
        "https?://github\\.com/[^/]+/[^/]+(/)?(.git)?",
        "GitHub Repository"
    ),
    GITHUB_ISSUE(
        "https?://github\\.com/[^/]+/[^/]+/issues/\\d+",
        "GitHub Issue"
    ),
    GITHUB_PR(
        "https?://github\\.com/[^/]+/[^/]+/pull/\\d+",
        "GitHub Pull Request"
    ),
    STACKOVERFLOW(
        "https?://stackoverflow\\.com/questions/\\d+",
        "StackOverflow Question"
    );

    private final Pattern pattern;
    private final String description;

    LinkType(String regex, String description) {
        this.pattern = Pattern.compile(regex);
        this.description = description;
    }

    public static LinkType fromUrl(String url) {
        for (LinkType type : values()) {
            if (type.pattern.matcher(url).matches()) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неподдерживаемый тип ссылки: " + url);
    }

    public String getDescription() {
        return description;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
