package backend.academy.scrapper.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public class UpdateDto {
    private Long id;
    private Long linkId;
    private Instant occurredAt;
    private JsonNode payload;
    private boolean sent;

    public UpdateDto(Long id, Long linkId, Instant occurredAt, JsonNode payload, boolean sent) {
        this.id = id;
        this.linkId = linkId;
        this.occurredAt = occurredAt;
        this.payload = payload;
        this.sent = sent;
    }

    public UpdateDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
