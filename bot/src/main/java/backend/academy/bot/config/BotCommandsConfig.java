package backend.academy.bot.config;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(BotCommandConfigProperties.class)
public class BotCommandsConfig {
    private final TelegramBot telegramBot;
    private final BotCommandConfigProperties commandsConfig;

    @PostConstruct
    public void init() {
        registerCommands();
    }

    private void registerCommands() {
        List<BotCommand> commands = commandsConfig.getCommands().stream()
            .map(cmd -> new BotCommand(cmd.getCommand(), cmd.getDescription()))
            .toList();

        SetMyCommands setCommands = new SetMyCommands(commands.toArray(new BotCommand[0]));
        telegramBot.execute(setCommands);
    }

}

