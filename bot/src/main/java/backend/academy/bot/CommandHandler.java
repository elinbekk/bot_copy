package backend.academy.bot;

import backend.academy.bot.dto.LinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.service.BotService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import static backend.academy.bot.BotMessages.FORMAT_LIST_MESSAGE;
import static backend.academy.bot.BotMessages.HELP_MESSAGE;
import static backend.academy.bot.BotMessages.LIST_EMPTY_MESSAGE;
import static backend.academy.bot.BotMessages.LIST_MESSAGE;
import static backend.academy.bot.BotMessages.START_MESSAGE;
import static backend.academy.bot.BotMessages.UNKNOWN_COMMAND_MESSAGE;

@Component
public class CommandHandler {
    private final BotService botService;
    private final ScrapperClient scrapperClient;
    private final InputParser inputParser;
    private final Map<Long, BotState> botStates = new ConcurrentHashMap<>();
    private final Map<Long, SessionData> sessions = new ConcurrentHashMap<>();

    public CommandHandler(BotService botService, ScrapperClient scrapperClient, InputParser inputParser) {
        this.botService = botService;
        this.scrapperClient = scrapperClient;
        this.inputParser = inputParser;
    }

    public void handleState(long chatId, String message) {
        BotState state = botStates.getOrDefault(chatId, BotState.INITIAL);
        switch (state) {
            case INITIAL -> handleInitial(chatId, message);
            case WAITING_FOR_LINK -> handleLink(chatId, message);
            case WAITING_FOR_TAGS -> handleTags(chatId, message);
            case WAITING_FOR_FILTERS -> handleFilters(chatId, message);
            case WAITING_FOR_UNTRACK_LINK -> handleUntrack(chatId, message);
        }
    }

    private void handleInitial(long chatId, String msg) {
        switch (msg) {
            case "/start" -> botService.sendMessage(chatId, START_MESSAGE);
            case "/help" -> botService.sendMessage(chatId, HELP_MESSAGE);
            case "/track" -> startTracking(chatId);
            case "/untrack" -> startUntracking(chatId);
            case "/list" -> processListCommand(chatId);

            default -> botService.sendMessage(chatId, UNKNOWN_COMMAND_MESSAGE);
        }
    }

    private void processListCommand(long chatId) {
        List<LinkResponse> links = scrapperClient.getListLinks(chatId);
        if (links.isEmpty()) {
            botService.sendMessage(chatId, LIST_EMPTY_MESSAGE);
        } else {
            String response = links.stream()
                .map(this::formatResource)
                .collect(Collectors.joining("\n\n"));
            botService.sendMessage(chatId, LIST_MESSAGE + response);
        }
    }

    public String formatResource(LinkResponse resource) {
        String filtersStr = resource.getFilters().entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", "));
        return String.format(
            FORMAT_LIST_MESSAGE,
            resource.getLink(),
            resource.getTags().isEmpty() ? "нет" : String.join(", ", resource.getTags()),
            resource.getFilters().isEmpty() ? "нет" : filtersStr);
    }

    private void startTracking(long chatId) {
        botStates.put(chatId, BotState.WAITING_FOR_LINK);
        sessions.put(chatId, new SessionData());
        botService.sendMessage(chatId, "Введите ссылку для отслеживания:");
    }

    private void handleLink(long chatId, String url) {
        sessions.get(chatId).setUrl(url);
        botStates.put(chatId, BotState.WAITING_FOR_TAGS);
        botService.sendMessage(chatId, "Введите тэги (через пробел или пустой):");
    }

    private void handleTags(long chatId, String input) {
        Set<String> tags = inputParser.parseTags(input);
        sessions.get(chatId).setTags(tags);
        botStates.put(chatId, BotState.WAITING_FOR_FILTERS);
        botService.sendMessage(chatId, "Введите фильтры (key:value через пробел или пустой):");
    }

    private void handleFilters(long chatId, String input) {
        Map<String, String> filters = inputParser.parseFilters(input);
        SessionData sd = sessions.get(chatId);
        sd.setFilters(filters);
        var req = new LinkRequest(sd.getUrl(), sd.getTags(), sd.getFilters());

        LinkResponse linkResponse = scrapperClient.addLink(chatId, req);
        botService.sendMessage(chatId, "Ссылка добавлена: " + linkResponse.getLink());
        resetState(chatId);
    }

    private void handleUntrack(long chatId, String url) {
        var req = new LinkRequest(url, null, null);
        CompletableFuture.runAsync(() -> scrapperClient.removeLink(chatId, req))
            .thenRun(() -> botService.sendMessage(chatId, "Ссылка удалена: " + url))
            .exceptionally(err -> {
                botService.sendMessage(chatId, "Ошибка: " + err.getMessage());
                return null;
            });

        resetState(chatId);
    }

    private void startUntracking(long chatId) {
        botStates.put(chatId, BotState.WAITING_FOR_UNTRACK_LINK);
        botService.sendMessage(chatId, "Введите ссылку для удаления:");
        }

    private void resetState(Long chatId) {
        botStates.remove(chatId);
        sessions.remove(chatId);
    }

    static class SessionData {
        String url;
        Set<String> tags;
        Map<String, String> filters;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        public Map<String, String> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, String> filters) {
            this.filters = filters;
        }
    }
}


//private void resetUserState(long chatId) {
//        botStates.remove(chatId);
//    }
//
//    private void startUntrackingProcess(long chatId) {
//        botStates.put(chatId, BotState.WAITING_FOR_UNTRACK_LINK);
//        botService.sendMessage(chatId, "Введите ссылку для удаления:");
//    }
//
//    private void showHelp(long chatId) {
//        botService.sendMessage(chatId, HELP_MESSAGE);
//    }
//
//    private void handleUnknownCommand(long chatId) {
//        botService.sendMessage(chatId, UNKNOWN_COMMAND_MESSAGE);
//    }
//}

