package backend.academy.scrapper.dto;

import java.util.List;

public class LinkUpdate {
    private String link;
    private String description;
    private List<Long> tgChatIds;

    public LinkUpdate(String link, String description, List<Long> tgChatIds) {
        this.link = link;
        this.description = description;
        this.tgChatIds = tgChatIds;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public List<Long> getTgChatIds() {
        return tgChatIds;
    }
}
