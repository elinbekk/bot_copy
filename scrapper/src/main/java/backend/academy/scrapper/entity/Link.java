package backend.academy.scrapper.entity;

import backend.academy.bot.entity.LinkType;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public class Link {
    private Long linkId;
    private String url;
    private LinkType resourceType;
    private Set<String> tags;
    private Map<String, String> filters;
    private Instant lastCheckedTime = Instant.now();

    public Link(Long linkId, String url, LinkType resourceType, Set<String> tags, Map<String, String> filters, Instant lastCheckedTime) {
        this.linkId = linkId;
        this.url = url;
        this.resourceType = resourceType;
        this.tags = tags;
        this.filters = filters;
        this.lastCheckedTime = lastCheckedTime;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LinkType getResourceType() {
        return resourceType;
    }

    public void setResourceType(LinkType resourceType) {
        this.resourceType = resourceType;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    public Instant getLastCheckedTime() {
        return lastCheckedTime;
    }

    public void setLastCheckedTime(Instant lastCheckedTime) {
        this.lastCheckedTime = lastCheckedTime;
    }
}
