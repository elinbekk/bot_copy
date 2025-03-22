package backend.academy.bot;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CommandHandler {
    private final BotService botService;
    private final LinkRepository linkRepository;
    private final Map<Long, BotState> userStates = new HashMap<>();
    private final Map<Long, TrackedResource> trackResources = new HashMap<>();

    public CommandHandler(BotService botService, LinkRepository linkRepository) {
        this.botService = botService;
        this.linkRepository = linkRepository;
    }

    public void handleCommand(long chatId, String message) {
        BotState state = userStates.getOrDefault(chatId, BotState.INITIAL);

        try {
            switch (state) {
                case INITIAL -> handleInitialState(chatId, message);
                case WAITING_FOR_LINK -> handleLinkInput(chatId, message);
                case WAITING_FOR_TAGS -> handleTagsInput(chatId, message);
                case WAITING_FOR_FILTERS -> handleFiltersInput(chatId, message);
                case WAITING_FOR_UNTRACK_LINK -> handleUntrackLink(chatId, message);
            }
        } catch (Exception e) {
            botService.sendMessage(chatId, "Ошибка: " + e.getMessage());
            resetUserState(chatId);
        }
    }

    private void handleInitialState(long chatId, String message) {
        switch (message) {
            case "/start" -> handleStartCommand(chatId);
            case "/track" -> startTrackingProcess(chatId);
            case "/untrack" -> startUntrackingProcess(chatId);
            case "/list" -> showTrackedLinks(chatId);
            case "/help" -> showHelp(chatId);
            default -> handleUnknownCommand(chatId);
        }
    }

    private void handleLinkInput(long chatId, String message) {
        validateUrl(message);
        TrackedResource resource = trackResources.get(chatId);
        resource.setLink(message);
        resource.setChatId(chatId);

        userStates.put(chatId, BotState.WAITING_FOR_TAGS);
        botService.sendMessage(chatId, "Введите теги через пробел (опционально):");
    }

    private void handleTagsInput(long chatId, String message) {
        TrackedResource resource = trackResources.get(chatId);
        resource.setTags(parseTags(message));

        userStates.put(chatId, BotState.WAITING_FOR_FILTERS);
        botService.sendMessage(chatId, "Введите фильтры в формате key:value (опционально):");
    }

    private void handleFiltersInput(long chatId, String message) {
        TrackedResource resource = trackResources.get(chatId);
        resource.setFilters(parseFilters(message));
        resource.setLastCheckedTime(Instant.now());

        saveTrackedResource(chatId, resource);
        resetUserState(chatId);
        botService.sendMessage(chatId, "Ссылка успешно добавлена!");
    }


    private void handleUntrackLink(long chatId, String message) {
        linkRepository.removeLink(chatId, message);
        botService.sendMessage(chatId, "Ссылка успешно удалена!");
        resetUserState(chatId);
    }

    private void saveTrackedResource(long chatId, TrackedResource resource) {
        if (linkRepository.existsByChatIdAndLink(chatId, resource.getLink())) {
            throw new IllegalStateException("Эта ссылка уже отслеживается");
        }
        linkRepository.addLink(chatId, resource);
    }

    private void showTrackedLinks(long chatId) {
        List<TrackedResource> resources = linkRepository.getLinks(chatId);

        if (resources.isEmpty()) {
            botService.sendMessage(chatId, "Список отслеживаемых ссылок пуст");
            return;
        }

        String response = resources.stream()
            .map(r -> formatResource(r))
            .collect(Collectors.joining("\n\n"));

        botService.sendMessage(chatId, "Ваши отслеживаемые ссылки:\n" + response);
    }

    private String formatResource(TrackedResource resource) {
        return String.format(
            "• %s\nТеги: %s\nФильтры: %s\nПоследняя проверка: %s",
            resource.getLink(),
            resource.getTags().isEmpty() ? "нет" : String.join(", ", resource.getTags()),
            resource.getFilters().isEmpty() ? "нет" : resource.getFilters(),
            DateTimeFormatter.ISO_INSTANT.format(resource.getLastCheckedTime())
        );
    }

    private void resetUserState(long chatId) {
        userStates.remove(chatId);
        trackResources.remove(chatId);
    }

    private Set<String> parseTags(String message) {
        return Arrays.stream(message.split("\\s+"))
            .filter(tag -> !tag.isBlank())
            .collect(Collectors.toSet());
    }

    private String parseFilters(String message) {
        return message;
    }

    private void validateUrl(String url) {
        if (!url.startsWith("http")) {
            throw new IllegalArgumentException("Некорректный формат ссылки");
        }
    }


    private void handleStartCommand(long chatId) {
        botService.sendMessage(chatId, """
            Привет! Я помогу отслеживать изменения на GitHub и Stack Overflow.
            Доступные команды:
            /track - начать отслеживание ссылки
            /untrack - прекратить отслеживание
            /list - показать отслеживаемые ссылки
            /help - показать справку""");
    }

    private void startTrackingProcess(long chatId) {
        userStates.put(chatId, BotState.WAITING_FOR_LINK);
        trackResources.put(chatId, new TrackedResource());
        botService.sendMessage(chatId, "Введите ссылку для отслеживания:");
    }

    private void startUntrackingProcess(long chatId) {
        userStates.put(chatId, BotState.WAITING_FOR_UNTRACK_LINK);
        botService.sendMessage(chatId, "Введите ссылку для удаления:");
    }

    private LinkType detectLinkType(String url) {
        if (url.contains("github.com")) return LinkType.GITHUB;
        if (url.contains("stackoverflow.com")) return LinkType.STACKOVERFLOW;
        throw new IllegalArgumentException("Неподдерживаемый тип ссылки");
    }

    private void showHelp(long chatId) {
        botService.sendMessage(chatId, """
            Доступные команды:
            /track - добавить ссылку
            /untrack - удалить ссылку
            /list - показать все ссылки
            /help - показать справку""");
    }

    private void handleUnknownCommand(long chatId) {
        botService.sendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд");
    }
}
