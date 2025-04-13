package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.entity.TrackedResource;
import backend.academy.bot.service.BotService;
import backend.academy.bot.service.TrackedResourceService;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import static backend.academy.bot.BotMessages.HELP_MESSAGE;
import static backend.academy.bot.BotMessages.LINK_DUPLICATED_MESSAGE;
import static backend.academy.bot.BotMessages.LINK_NOT_FOUND_MESSAGE;
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
    private final Map<Long, BotState> botStates = new HashMap<>();
    private final Map<Long, TrackedResource> trackResources = new ConcurrentHashMap<>();
    private final InputParser inputParser = new InputParser();
    private final TrackedResourceService trackedResourceService;
    private final ResourceTypeDetector resourceTypeDetector;

    public CommandHandler(BotService botService, TrackedResourceService trackedResourceService, ResourceTypeDetector resourceTypeDetector) {
        this.botService = botService;
        this.trackedResourceService = trackedResourceService;
        this.resourceTypeDetector = resourceTypeDetector;
    }

    public void handleState(long chatId, String message) {
        BotState state = botStates.getOrDefault(chatId, BotState.INITIAL);
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
            case "/start" -> processStartCommand(chatId);
            case "/track" -> startTrackingProcess(chatId);
            case "/untrack" -> startUntrackingProcess(chatId);
            case "/list" -> trackedResourceService.showTrackedLinks(chatId);
            case "/help" -> showHelp(chatId);
            default -> handleUnknownCommand(chatId);
        }
    }

    private void processStartCommand(long chatId) {
        botService.sendMessage(chatId, START_MESSAGE);
    }

    private void startTrackingProcess(long chatId) {
        botStates.put(chatId, BotState.WAITING_FOR_LINK);
        trackResources.put(chatId, new TrackedResource());
        botService.sendMessage(chatId, WAITING_FOR_LINK_MESSAGE);
    }

    private void handleLinkInput(long chatId, String message) {
        try {
            trackedResourceService.processInvalidURL(message);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException();
        }
        if (trackedResourceService.isResourceAlreadyTracked(chatId, message)) {
            throw new IllegalStateException(LINK_DUPLICATED_MESSAGE);
        }
        TrackedResource resource = trackResources.get(chatId);
        LinkType linkType = resourceTypeDetector.detectResourceType(message);
        resource.setLink(message);
        resource.setLinkType(linkType);
        resource.setChatId(chatId);

        botStates.put(chatId, BotState.WAITING_FOR_TAGS);
        botService.sendMessage(chatId, WAITING_FOR_TAGS_MESSAGE);
    }

    private void handleTagsInput(long chatId, String message) {
        TrackedResource resource = trackResources.get(chatId);
        Set<String> tagsFromMessage = inputParser.parseTags(message);
        if (!message.equals("-")) {
            resource.setTags(tagsFromMessage);
        }
        botStates.put(chatId, BotState.WAITING_FOR_FILTERS);
        botService.sendMessage(chatId, WAITING_FOR_FILTERS_MESSAGE);
    }

    private void handleFiltersInput(long chatId, String message) {
        TrackedResource resource = trackResources.get(chatId);
        if (!message.equals("-")) {
            resource.setFilters(inputParser.parseFilters(message));
        }
        resource.setLastCheckedTime(Instant.now());
        trackedResourceService.saveTrackedResource(chatId, resource);
        resetUserState(chatId);
        botService.sendMessage(chatId, TRACK_MESSAGE);
    }

    private void handleUntrackLink(long chatId, String message) {
        if (!trackedResourceService.isResourceAlreadyTracked(chatId, message)) {
            throw new IllegalArgumentException(LINK_NOT_FOUND_MESSAGE);
        }
        trackedResourceService.removeResource(chatId, message);
        botService.sendMessage(chatId, UNTRACK_MESSAGE);

        resetUserState(chatId);
    }

    private void resetUserState(long chatId) {
        botStates.remove(chatId);
        trackResources.remove(chatId);
    }

    private void startUntrackingProcess(long chatId) {
        botStates.put(chatId, BotState.WAITING_FOR_UNTRACK_LINK);
        botService.sendMessage(chatId, "Введите ссылку для удаления:");
    }

    private void showHelp(long chatId) {
        botService.sendMessage(chatId, HELP_MESSAGE);
    }

    private void handleUnknownCommand(long chatId) {
        botService.sendMessage(chatId, UNKNOWN_COMMAND_MESSAGE);
    }
}
