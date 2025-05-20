package backend.academy.scrapper.dto;

import java.util.Map;
import java.util.Set;

public class LinkResponse {
    private Long id;
    private String link;
    private Set<String> tags;
    private Map<String, String> filters;
    private String lastCheckedTime;

    public LinkResponse() {}

    public LinkResponse(String url, Set<String> tags, Map<String, String> filters, String lastCheckedTime) {
        this.link = url;
        this.tags = tags;
        this.filters = filters;
        this.lastCheckedTime = lastCheckedTime;
    }

    public LinkResponse(Long id, String url, Set<String> tags, Map<String, String> filters, String lastCheckedTime) {
        this.id = id;
        this.link = url;
        this.tags = tags;
        this.filters = filters;
        this.lastCheckedTime = lastCheckedTime;
    }

    public Long getId() {
        return id;
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

    public String getLastCheckedTime() {
        return lastCheckedTime;
    }
}
