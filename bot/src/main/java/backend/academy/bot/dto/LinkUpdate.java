package backend.academy.bot.dto;

import java.util.List;

public class LinkUpdate {
    private String link;
    private String description;
    private List<Long> tgChatIds;

    public LinkUpdate() {}

    public LinkUpdate(String link, String description, List<Long> tgChatIds) {}

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
