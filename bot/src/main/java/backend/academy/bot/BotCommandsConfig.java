package backend.academy.bot;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

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

//    public BotConfig(@Value("${telegram.bot.token}") String token) {
//        this.telegramBot = new TelegramBot(token);
//    }

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

