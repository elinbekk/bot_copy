package backend.academy.bot.dto;

import java.util.Map;
import java.util.Set;

public class LinkRequest {
    private String url;
    private Set<String> tags;
    private Map<String, String> filters;

    public LinkRequest(String url, Set<String> tags, Map<String, String> filters) {
        this.url = url;
        this.tags = tags;
        this.filters = filters;
    }

    public String getUrl() {
        return url;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Map<String, String> getFilters() {
        return filters;
    }
}
