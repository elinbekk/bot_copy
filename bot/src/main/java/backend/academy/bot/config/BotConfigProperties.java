package backend.academy.bot.config;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record BotConfigProperties(List<Command> commands, @NotBlank String telegramToken) {
    public record Command(@NotBlank String command, @NotBlank String description) {}
}
