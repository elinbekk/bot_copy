package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(@NotEmpty String githubToken, StackOverflowCredentials stackOverflow,
                             @NotEmpty String baseUrl) {
    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {
    }

    @Bean
    public WebClient webClient() {
        return WebClient.create(baseUrl);
    }

    @Bean
    public BotClient botClient(WebClient webClient) {
        return new BotClient(webClient);
    }
}


