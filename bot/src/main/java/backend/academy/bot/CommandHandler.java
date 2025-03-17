package backend.academy.bot;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CommandHandler {
    private final BotService botService;

    private final Map<Long, BotState> userStates = new HashMap<>();
    private final Map<Long, String> userLinks = new HashMap<>();

    public CommandHandler(BotService botService) {
        this.botService = botService;
    }

    public void handleCommand(long chatId, String message) {
        BotState state = userStates.getOrDefault(chatId, BotState.INITIAL);

        switch (state) {
            case INITIAL -> {
                if (message.equals("/start")) {
                    botService.sendMessage(chatId, "Привет! Я твой трекер. Используй /track для отслеживания ссылки");
                } else if (message.equals("/track")) {
                    userStates.put(chatId, BotState.WAITING_FOR_LINK);
                    botService.sendMessage(chatId, "Введите ссылку для отслеживания:");
                } else if (message.equals("/list")) {
                    showTrackedLinks(chatId);
                } else if (message.equals ("/untrack")) {
                    userStates.put(chatId, BotState.WAITING_FOR_LINK); //todo: add new state
                    botService.sendMessage(chatId, "Введите ссылку для удаления:");
                }else {
                    botService.sendMessage(chatId, "Неизвестная команда. Используй /help");
                }
            }
            case WAITING_FOR_LINK -> {
                userLinks.put(chatId, message);
                userStates.put(chatId, BotState.WAITING_FOR_TAGS);
                botService.sendMessage(chatId, "Введите теги (опционально):");
            }
            case WAITING_FOR_TAGS -> {
                userStates.put(chatId, BotState.WAITING_FOR_FILTERS);
                botService.sendMessage(chatId, "Настройте фильтры (опционально):");
            }
            case WAITING_FOR_FILTERS -> {
                userStates.put(chatId, BotState.INITIAL);
                botService.sendMessage(chatId, "Ссылка успешно добавлена в отслеживание");
            }
        }
    }

    private void showTrackedLinks(long chatId) {
        botService.sendMessage(chatId, "Ваши отслеживаемые ссылки: " + userLinks.getOrDefault(chatId, "Пока ничего нет."));
    }
}
