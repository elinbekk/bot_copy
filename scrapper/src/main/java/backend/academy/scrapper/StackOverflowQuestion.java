package backend.academy.scrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StackOverflowQuestion {
    @JsonProperty("last_activity_date")
    private long lastActivityDate;

    @JsonProperty("title")
    private String title;

    public StackOverflowQuestion(long lastActivity) {
        this.lastActivityDate = lastActivity;
    }

    public long getLastActivityDate() {
        return lastActivityDate;
    }
    public void setLastActivityDate(long lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
