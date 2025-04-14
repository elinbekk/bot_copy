package backend.academy.scrapper.config;

import backend.academy.scrapper.client.BotClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;

@Configuration
@Validated
@EnableConfigurationProperties(ScrapperConfig.class)
public class ClientConfig {
    private final ScrapperConfig scrapperConfig;

    public ClientConfig(ScrapperConfig scrapperConfig) {
        this.scrapperConfig = scrapperConfig;
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
            .baseUrl(scrapperConfig.baseUrl())
            .defaultHeader("Accept", "application/json")
            .requestInterceptor((request, body, execution) -> {
                return execution.execute(request, body);
            })
            .build();
    }

    @Bean
    public BotClient botClient(RestClient restClient) {
        return new BotClient(restClient);
    }
}
