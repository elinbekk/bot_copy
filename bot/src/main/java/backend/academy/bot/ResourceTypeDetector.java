package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceTypeDetector {
    public LinkType detectResourceType(String url) {
        for (ResourceTypePattern typePattern : ResourceTypePattern.values()) {
            if (typePattern.getPattern().matcher(url).find()) {
                return mapToLinkType(typePattern);
            }
        }
        throw new IllegalArgumentException("Unsupported link type: " + url);
    }

    private LinkType mapToLinkType(ResourceTypePattern pattern) {
        return switch (pattern) {
            case GITHUB_ISSUE -> LinkType.GITHUB_ISSUE;
            case GITHUB_PR -> LinkType.GITHUB_PR;
            case GITHUB_REPO -> LinkType.GITHUB_REPO;
            case STACKOVERFLOW -> LinkType.STACKOVERFLOW;
        };
    }
}
