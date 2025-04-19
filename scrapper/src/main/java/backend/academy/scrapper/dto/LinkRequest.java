package backend.academy.scrapper.dto;

import backend.academy.scrapper.entity.LinkType;
import java.util.Map;
import java.util.Set;

public class LinkRequest {
    private String link;
    private LinkType linkType;
    private Set<String> tags;
    private Map<String, String> filters;

    public LinkRequest() {}

    public LinkRequest(String link, Set<String> tags, Map<String, String> filters) {
        this.link = link;
        this.tags = tags;
        this.filters = filters;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public String getLink() {
        return link;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Map<String, String> getFilters() {
        return filters;
    }
}
