package backend.academy.bot;

import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteWebhook;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.stereotype.Component;


@Component
public class Bot implements UpdatesListener, ExceptionHandler {
    private final TelegramBot telegramBot;
    private final CommandHandler commandHandler;

    public Bot(TelegramBot telegramBot, CommandHandler commandHandler) {
        this.telegramBot = telegramBot;
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void start() {
        telegramBot.setUpdatesListener(this, this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null && update.message().text() != null) {
                long chatId = update.message().chat().id();
                String message = update.message().text();
//                System.out.println("Получено сообщение: " + message);
                commandHandler.handleCommand(chatId, message);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Override
    public void onException(TelegramException e) {
        if (e.response() != null) {
//            log.error("Telegram error: {} - {}", e.response().errorCode(), e.response().description());
            System.out.println(e.response());
        } else {
//            log.error(e.toString());
            e.printStackTrace();
        }
    }
}




