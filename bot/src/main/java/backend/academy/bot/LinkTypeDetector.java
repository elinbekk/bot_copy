package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import org.springframework.stereotype.Component;

@Component
public class LinkTypeDetector {
    public LinkType detectResourceType(String url) {
        return LinkType.fromUrl(url);
    }
}
