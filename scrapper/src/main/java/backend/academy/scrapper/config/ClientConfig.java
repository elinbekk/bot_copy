package backend.academy.scrapper.config;

import backend.academy.scrapper.client.BotClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;

@Validated
@Configuration
@EnableConfigurationProperties({ScrapperConfig.class, GithubProperties.class, StackoverflowProperties.class})
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

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }
}
