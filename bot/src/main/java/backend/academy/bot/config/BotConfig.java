package backend.academy.bot.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(BotConfigProperties.class)
public class BotConfig {
    @Bean
    public TelegramBot telegramBot(BotConfigProperties botConfigProperties) {
        return new TelegramBot(botConfigProperties.telegramToken());
    }

    @Bean
    public BotCommandsConfig botCommandsConfig(TelegramBot bot, BotConfigProperties properties) {
        return new BotCommandsConfig(bot, properties);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
