package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.clock")
public class ClockProperties {
    @NotEmpty
    private String timeZone = ZoneId.systemDefault().getId();

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
