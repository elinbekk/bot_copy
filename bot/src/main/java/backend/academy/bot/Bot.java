package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class Bot {
    private final CommandHandler commandHandler;

    public Bot(TelegramBot telegramBot, CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        telegramBot.setUpdatesListener(this::handleUpdates);
    }

    private int handleUpdates(List<Update> updates) {
        for (Update update : updates) {
            onUpdateReceived(update);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public void onUpdateReceived(Update update) {
        if (update.message() != null && update.message().text() != null) {
            long chatId = update.message().chat().id();
            String message = update.message().text();
            commandHandler.handleState(chatId, message);
        }
    }
}




