package backend.academy.bot.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ClockProperties.class)
public class TimeConfig {
    private final ClockProperties clockProperties;

    public TimeConfig(ClockProperties clockProperties) {
        this.clockProperties = clockProperties;
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(clockProperties.getTimeZone()));
    }
}
