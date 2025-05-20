package backend.academy.bot.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(BotConfigProperties.class)
public class BotConfig {
    private final BotConfigProperties botConfigProperties;

    public BotConfig(BotConfigProperties botConfigProperties) {
        this.botConfigProperties = botConfigProperties;
    }

    @Bean
    public TelegramBot telegramBot(BotConfigProperties botConfigProperties) {
        return new TelegramBot(botConfigProperties.telegramToken());
    }

    @Bean
    public BotCommandsConfig botCommandsConfig(TelegramBot bot, BotConfigProperties properties) {
        return new BotCommandsConfig(bot, properties);
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(botConfigProperties.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
