package backend.academy.bot.dto;

import java.util.Map;
import java.util.Set;

public class LinkResponse {
    private Long id;
    private String link;
    private Set<String> tags;
    private Map<String, String> filters;
    private String lastCheckedTime;

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
