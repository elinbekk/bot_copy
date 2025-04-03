package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.entity.TrackedResource;
import backend.academy.bot.repository.LinkRepository;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import static backend.academy.bot.BotMessages.HELP_MESSAGE;
import static backend.academy.bot.BotMessages.LINK_DUPLICATED_MESSAGE;
import static backend.academy.bot.BotMessages.LIST_EMPTY_MESSAGE;
import static backend.academy.bot.BotMessages.LIST_MESSAGE;
import static backend.academy.bot.BotMessages.START_MESSAGE;
import static backend.academy.bot.BotMessages.TRACK_MESSAGE;
import static backend.academy.bot.BotMessages.UNKNOWN_COMMAND_MESSAGE;
import static backend.academy.bot.BotMessages.UNTRACK_MESSAGE;
import static backend.academy.bot.BotMessages.WAITING_FOR_FILTERS_MESSAGE;
import static backend.academy.bot.BotMessages.WAITING_FOR_LINK_MESSAGE;
import static backend.academy.bot.BotMessages.WAITING_FOR_TAGS_MESSAGE;

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

    private void handleInitialState(long chatId, String command) {
        switch (command) {
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
        if (linkRepository.existsByChatIdAndLink(chatId, message)) {
            throw new IllegalStateException("Ссылка уже отслеживается");
        }
        TrackedResource resource = trackResources.get(chatId);
        LinkType linkType = detectLinkType(message);
        resource.setLink(message);
        resource.setLinkType(linkType);
        resource.setChatId(chatId);

        userStates.put(chatId, BotState.WAITING_FOR_TAGS);
        botService.sendMessage(chatId, WAITING_FOR_TAGS_MESSAGE);
    }

    private void handleTagsInput(long chatId, String message) {
        TrackedResource resource = trackResources.get(chatId);
        if (!message.equals("-")) {
            resource.setTags(parseTags(message));
        }
        userStates.put(chatId, BotState.WAITING_FOR_FILTERS);
        botService.sendMessage(chatId, WAITING_FOR_FILTERS_MESSAGE);
    }

    private void handleFiltersInput(long chatId, String message) {
        TrackedResource resource = trackResources.get(chatId);
        if (!message.equals("-")) {
            resource.setFilters(parseFilters(message));
        }
        resource.setLastCheckedTime(Instant.now());
        saveTrackedResource(chatId, resource);
        resetUserState(chatId);
        botService.sendMessage(chatId, TRACK_MESSAGE);
    }


    private void handleUntrackLink(long chatId, String message) {
        if (!linkRepository.existsByChatIdAndLink(chatId, message)) {
            throw new IllegalArgumentException("Ссылка не найдена в вашем списке отслеживания");
        }
        linkRepository.removeLink(chatId, message);
        botService.sendMessage(chatId, UNTRACK_MESSAGE);

        resetUserState(chatId);
    }

    void saveTrackedResource(long chatId, TrackedResource resource) {
        if (linkRepository.existsByChatIdAndLink(chatId, resource.getLink())) {
            botService.sendMessage(chatId, LINK_DUPLICATED_MESSAGE);
            throw new IllegalArgumentException();
        }
        linkRepository.addLink(chatId, resource);
    }

    private void showTrackedLinks(long chatId) {
        List<TrackedResource> resources = linkRepository.getLinks(chatId);

        if (resources.isEmpty()) {
            botService.sendMessage(chatId, LIST_EMPTY_MESSAGE);
            return;
        }

        String response = resources.stream()
            .map(r -> formatResource(r))
            .collect(Collectors.joining("\n\n"));

        botService.sendMessage(chatId, LIST_MESSAGE + response);
    }

    String formatResource(TrackedResource resource) {
        String filtersStr = resource.getFilters().entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", "));
        return String.format(
            "• %s\nТеги: %s\nФильтры: %s\nПоследняя проверка: %s",
            resource.getLink(),
            resource.getTags().isEmpty() ? "нет" : String.join(", ", resource.getTags()),
            resource.getFilters().isEmpty() ? "нет" : filtersStr,
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

    private Map<String, String> parseFilters(String message) {
        Map<String, String> filters = new HashMap<>();

        if (message == null || message.isBlank()) {
            return filters;
        }

        Arrays.stream(message.split("\\s+"))
            .filter(part -> !part.isBlank())
            .forEach(part -> {
                String[] keyValue = part.split(":", 2);
                if (keyValue.length != 2) {
                    throw new IllegalArgumentException(
                        "Некорректный формат фильтра: " + part + "\n" +
                            "Используйте формат: ключ:значение"
                    );
                }
                filters.put(keyValue[0].trim(), keyValue[1].trim());
            });

        return filters;
    }

    private void validateUrl(String url) {
        if (!url.startsWith("http")) {
            throw new IllegalArgumentException("Некорректный формат ссылки");
        }
    }

    private void handleStartCommand(long chatId) {
        botService.sendMessage(chatId, START_MESSAGE);
    }

    private void startTrackingProcess(long chatId) {
        userStates.put(chatId, BotState.WAITING_FOR_LINK);
        trackResources.put(chatId, new TrackedResource());
        botService.sendMessage(chatId, WAITING_FOR_LINK_MESSAGE);
    }

    private void startUntrackingProcess(long chatId) {
        userStates.put(chatId, BotState.WAITING_FOR_UNTRACK_LINK);
        botService.sendMessage(chatId, "Введите ссылку для удаления:");
    }

    protected LinkType detectLinkType(String url) {
        if (url.contains("github.com")) {
            Pattern issuePattern = Pattern.compile("https?://github\\.com/[^/]+/[^/]+/issues/\\d+");
            Pattern prPattern = Pattern.compile("https?://github\\.com/[^/]+/[^/]+/pull/\\d+");
            Pattern repoPattern = Pattern.compile("https?://github\\.com/[^/]+/[^/]+(/)?(.git)?");

            if (issuePattern.matcher(url).find()) {
                return LinkType.GITHUB_ISSUE;
            } else if (prPattern.matcher(url).find()) {
                return LinkType.GITHUB_PR;
            } else if (repoPattern.matcher(url).find()) {
                return LinkType.GITHUB_REPO;
            }
        }
        if (url.contains("stackoverflow.com")) {
            return LinkType.STACKOVERFLOW;
        }
        throw new IllegalArgumentException("Неподдерживаемый тип ссылки");
    }

    private void showHelp(long chatId) {
        botService.sendMessage(chatId, HELP_MESSAGE);
    }

    private void handleUnknownCommand(long chatId) {
        botService.sendMessage(chatId, UNKNOWN_COMMAND_MESSAGE);
    }
}
