package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StackOverflowQuestion {
    @JsonProperty("last_activity_date")
    private long lastActivityDate;

    @JsonProperty("title")
    private String title;

    @JsonCreator
    public StackOverflowQuestion(
        @JsonProperty("last_activity_date") long lastActivityDate,
        @JsonProperty("title") String title
    ) {
        this.lastActivityDate = lastActivityDate;
        this.title = title;
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
