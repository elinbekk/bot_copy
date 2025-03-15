package backend.academy.scrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubResponse(@JsonProperty("updated_at") String updatedAt,
                             @JsonProperty("full_name") String fullName) {
}
