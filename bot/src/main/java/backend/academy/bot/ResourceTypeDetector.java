package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceTypeDetector {
    public LinkType detectResourceType(String url) {
        return LinkType.fromUrl(url);
    }
}
