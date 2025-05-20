package backend.academy.scrapper.dto;

import backend.academy.scrapper.entity.LinkType;
import java.util.Map;
import java.util.Set;

public class Link {
    private Long linkId;
    private String url;
    private Long chatId;
    private LinkType linkType;
    private Set<String> tags;
    private Map<String, String> filters;
    private String lastCheckedTime;

    public Link() {}

    public Link(
            String url,
            Long chatId,
            LinkType resourceType,
            Set<String> tags,
            Map<String, String> filters,
            String lastCheckedTime) {
        this.url = url;
        this.chatId = chatId;
        this.linkType = resourceType;
        this.tags = tags;
        this.filters = filters;
        this.lastCheckedTime = lastCheckedTime;
    }

    public Link(
            Long linkId,
            String url,
            Long chatId,
            LinkType linkType,
            Set<String> tags,
            Map<String, String> filters,
            String lastCheckedTime) {
        this.linkId = linkId;
        this.url = url;
        this.chatId = chatId;
        this.linkType = linkType;
        this.tags = tags;
        this.filters = filters;
        this.lastCheckedTime = lastCheckedTime;
    }

    public Link(Long linkId) {
        this.linkId = linkId;
    }

    public Link(
            Long linkId,
            String url,
            LinkType linkType,
            Set<String> tags,
            Map<String, String> filters,
            String lastCheckedTime) {
        this.linkId = linkId;
        this.url = url;
        this.linkType = linkType;
        this.tags = tags;
        this.filters = filters;
        this.lastCheckedTime = lastCheckedTime;
    }

    public Link(Long id, String url, Long chatId, LinkType type) {
        this.linkId = id;
        this.url = url;
        this.chatId = chatId;
        this.linkType = type;
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

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
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

    public String getLastCheckedTime() {
        return lastCheckedTime;
    }

    public void setLastCheckedTime(String lastCheckedTime) {
        this.lastCheckedTime = lastCheckedTime;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}
