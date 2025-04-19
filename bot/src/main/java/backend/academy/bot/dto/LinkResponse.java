package backend.academy.bot.dto;

import java.util.Map;
import java.util.Set;

public class LinkResponse {
    private Long id;
    private String url;
    private Set<String> tags;
    private Map<String, String> filters;

    public Long getId() {
        return id;
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
