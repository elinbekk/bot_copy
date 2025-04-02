package backend.academy.bot.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class TrackedResource {
    private Long id;
    private Long chatId;
    private String link;
    private LinkType linkType;
    private Set<String> tags = new HashSet<>();
    private Map<String, String> filters;
    private Instant lastCheckedTime = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<String> getTags() {
        return tags != null ? tags : new HashSet<>();
    }

    public Map<String, String> getFilters() {
        return filters != null ? filters : new HashMap<>();
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

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }
}
