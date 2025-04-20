package backend.academy.bot.dto;

import backend.academy.bot.entity.LinkType;
import java.util.Map;
import java.util.Set;

public class LinkRequest {
    private String link;
    private LinkType linkType;
    private Set<String> tags;
    private Map<String, String> filters;

    public LinkRequest(String link, LinkType linkType, Set<String> tags, Map<String, String> filters) {
        this.link = link;
        this.linkType = linkType;
        this.tags = tags;
        this.filters = filters;
    }

    public String getLink() {
        return link;
    }


    public LinkType getLinkType() {
        return linkType;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Map<String, String> getFilters() {
        return filters;
    }
}
