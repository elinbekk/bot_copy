package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StackOverflowQuestion {
    @JsonProperty("last_activity_date")
    private long lastActivityDate;

    @JsonProperty("title")
    private String title;

    @JsonProperty("owner")
    private Owner owner;

    @JsonProperty("creation_date")
    private long creatingDate;

    @JsonProperty("body")
    private String body;

    @JsonCreator
    public StackOverflowQuestion(
        @JsonProperty("last_activity_date") long lastActivityDate,
        @JsonProperty("title") String title,
        @JsonProperty("owner") Owner owner,
        @JsonProperty("creating_date") long creatingDate,
        @JsonProperty("body") String body) {
        this.lastActivityDate = lastActivityDate;
        this.title = title;
        this.owner = owner;
        this.creatingDate = creatingDate;
        this.body = body;
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

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public long getCreatingDate() {
        return creatingDate;
    }

    public void setCreatingDate(long creatingDate) {
        this.creatingDate = creatingDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static class Owner {
        @JsonProperty("display_name")
        private String displayName;

        public String getDisplayName() {
            return displayName;
        }
    }
}
