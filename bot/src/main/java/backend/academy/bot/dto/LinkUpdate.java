package backend.academy.bot.dto;

import java.util.List;

public class LinkUpdate {
    private final String link;
    private final String description;
    private final List<Long> tgChatIds;

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
