package backend.academy.bot.config;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;

public class BotCommandsConfig {
    private final TelegramBot bot;
    private final BotConfigProperties config;

    public BotCommandsConfig(TelegramBot bot, BotConfigProperties config) {
        this.bot = bot;
        this.config = config;
    }

    @PostConstruct
    public void init() {
        List<BotCommand> commands = config.commands().stream()
                .map(c -> new BotCommand(c.command(), c.description()))
                .toList();

        bot.execute(new SetMyCommands(commands.toArray(BotCommand[]::new)));
    }
}
