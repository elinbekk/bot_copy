package backend.academy.bot.config;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotCommandsConfig {
    private final TelegramBot telegramBot;

    public BotCommandsConfig(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        registerCommands();
    }

    private void registerCommands() {
        List<BotCommand> commands = List.of(
            new BotCommand("start", "Начало работы"),
            new BotCommand("track", "Отслеживание ссылки"),
            new BotCommand("untrack", "Удаление ссылки"),
            new BotCommand("list", "Список ссылок"),
            new BotCommand("help", "Справка")
        );

        SetMyCommands setCommands = new SetMyCommands(commands.toArray(new BotCommand[0]));
        telegramBot.execute(setCommands);
    }
}

