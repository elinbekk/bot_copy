package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken) {
    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(telegramToken);
    }
    @Bean
    public WebClient botWebClient(@Value("${bot.base-url}") String baseUrl) {
        return WebClient.create(baseUrl);
    }
}
