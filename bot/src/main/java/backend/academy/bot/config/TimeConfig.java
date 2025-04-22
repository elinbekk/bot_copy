package backend.academy.bot.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.systemDefault());
        // return Clock.system(ZoneId.of("Europe/Moscow"));
    }
}
