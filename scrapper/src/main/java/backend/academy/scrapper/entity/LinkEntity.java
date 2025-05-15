package backend.academy.scrapper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "links")
public class LinkEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    private String url;
    private String type;
    private Instant lastChecked;

    @Column(columnDefinition = "jsonb")
    private String tags;

    @Column(columnDefinition = "jsonb")
    private String filters;

    public LinkEntity(ChatEntity chat, String url, String type, Instant lastChecked, String tags, String filters) {
        this.chat = chat;
        this.url = url;
        this.type = type;
        this.lastChecked = lastChecked;
        this.tags = tags;
        this.filters = filters;
    }

    public LinkEntity() {}

    public Long getId() {
        return id;
    }

    public ChatEntity getChat() {
        return chat;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public Instant getLastChecked() {
        return lastChecked;
    }

    public String getTags() {
        return tags;
    }

    public String getFilters() {
        return filters;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChat(ChatEntity chat) {
        this.chat = chat;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLastChecked(Instant lastChecked) {
        this.lastChecked = lastChecked;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }
}
