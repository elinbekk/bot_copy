package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.github")
public record GithubProperties(
    @NotEmpty String token,
    @NotEmpty String apiUrl
) {
}
