package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.stereotype.Component;

@Component
public class BotService {
    private final TelegramBot telegramBot;

    public BotService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendMessage(long chatId, String message) {
        SendResponse response = telegramBot.execute(new SendMessage(chatId, message));
        System.out.println("Response: " + response);
    }
}
