package backend.academy.bot;

import backend.academy.bot.constant.BotState;
import backend.academy.bot.dto.LinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.entity.LinkType;
import backend.academy.bot.exception.DuplicateLinkException;
import backend.academy.bot.exception.LinkNotFoundException;
import backend.academy.bot.helper.InputParser;
import backend.academy.bot.helper.LinkTypeDetector;
import backend.academy.bot.service.BotService;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import static backend.academy.bot.constant.BotMessages.FORMAT_LIST_MESSAGE;
import static backend.academy.bot.constant.BotMessages.HELP_MESSAGE;
import static backend.academy.bot.constant.BotMessages.LINK_DUPLICATED_MESSAGE;
import static backend.academy.bot.constant.BotMessages.LINK_INCORRECT_MESSAGE;
import static backend.academy.bot.constant.BotMessages.LINK_NOT_FOUND_MESSAGE;
import static backend.academy.bot.constant.BotMessages.LIST_EMPTY_MESSAGE;
import static backend.academy.bot.constant.BotMessages.LIST_MESSAGE;
import static backend.academy.bot.constant.BotMessages.START_MESSAGE;
import static backend.academy.bot.constant.BotMessages.UNKNOWN_COMMAND_MESSAGE;
import static backend.academy.bot.constant.BotMessages.WAITING_FOR_FILTERS_MESSAGE;
import static backend.academy.bot.constant.BotMessages.WAITING_FOR_LINK_MESSAGE;
import static backend.academy.bot.constant.BotMessages.WAITING_FOR_TAGS_MESSAGE;

@Component
public class CommandHandler {
    private final BotService botService;
    private final ScrapperClient scrapperClient;
    private final InputParser inputParser;
    private final LinkTypeDetector linkTypeDetector;
    private final Map<Long, BotState> botStates = new ConcurrentHashMap<>();
    private final Map<Long, SessionData> sessions = new ConcurrentHashMap<>();

    public CommandHandler(BotService botService, ScrapperClient scrapperClient, InputParser inputParser, LinkTypeDetector linkTypeDetector) {
        this.botService = botService;
        this.scrapperClient = scrapperClient;
        this.inputParser = inputParser;
        this.linkTypeDetector = linkTypeDetector;
    }

    public void handleState(long chatId, String message) {
        BotState state = botStates.getOrDefault(chatId, BotState.INITIAL);
        try {
            switch (state) {
                case INITIAL -> handleInitial(chatId, message);
                case WAITING_FOR_LINK -> handleLink(chatId, message);
                case WAITING_FOR_TAGS -> handleTags(chatId, message);
                case WAITING_FOR_FILTERS -> handleFilters(chatId, message);
                case WAITING_FOR_UNTRACK_LINK -> handleUntrack(chatId, message);
            }
        } catch (Exception e) {
            botService.sendMessage(chatId, "Ошибка:" + e.getMessage());
            resetState(chatId);
        }
    }

    private void handleInitial(long chatId, String message) {
//        scrapperClient.registerChat(chatId);
        switch (message) {
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
                .map(this::formatLink)
                .collect(Collectors.joining("\n\n"));
            botService.sendMessage(chatId, LIST_MESSAGE + response);
        }
        resetState(chatId);
    }

    public String formatLink(LinkResponse link) {
        String filtersStr = link.getFilters().entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", "));

        String formattedTime = getFormattedTime(link);

        return String.format(
            FORMAT_LIST_MESSAGE,
            link.getLink(),
            link.getTags().isEmpty() ? "нет" : String.join(", ", link.getTags()),
            link.getFilters().isEmpty() ? "нет" : filtersStr,
            formattedTime
        );
    }

    private @NotNull String getFormattedTime(LinkResponse link) {
        ZonedDateTime zonedDateTime = Instant.parse(link.getLastCheckedTime())
            .atZone(ZoneId.systemDefault());
        return DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm:ss z")
            .format(zonedDateTime);
    }

    private void startTracking(long chatId) {
        botStates.put(chatId, BotState.WAITING_FOR_LINK);
        sessions.put(chatId, new SessionData());
        botService.sendMessage(chatId, WAITING_FOR_LINK_MESSAGE);
    }

    private void handleLink(long chatId, String url) {
        try {
            isValidURL(url);
            sessions.get(chatId).setUrl(url);
            sessions.get(chatId).setLinkType(linkTypeDetector.detectResourceType(url));
            botStates.put(chatId, BotState.WAITING_FOR_TAGS);
            botService.sendMessage(chatId, WAITING_FOR_TAGS_MESSAGE);
        } catch (MalformedURLException | URISyntaxException e) {
            botService.sendMessage(chatId, LINK_INCORRECT_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    private void isValidURL(String url) throws MalformedURLException, URISyntaxException {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
        }
    }

    private void handleTags(long chatId, String message) {
        Set<String> tags = inputParser.parseTags(message);
        sessions.get(chatId).setTags(tags);
        botStates.put(chatId, BotState.WAITING_FOR_FILTERS);
        botService.sendMessage(chatId, WAITING_FOR_FILTERS_MESSAGE);
    }

    private void handleFilters(long chatId, String input) {
        Map<String, String> filters = inputParser.parseFilters(input);
        SessionData sd = sessions.get(chatId);
        sd.setFilters(filters);
        LinkRequest req = new LinkRequest(sd.getUrl(), sd.getLinkType(), sd.getTags(), sd.getFilters());

        try {
            LinkResponse linkResponse = scrapperClient.addLink(chatId, req);
            botService.sendMessage(chatId, "Ссылка добавлена: " + linkResponse.getLink());
        } catch (DuplicateLinkException e) {
            botService.sendMessage(chatId, LINK_DUPLICATED_MESSAGE);
        }
        resetState(chatId);
    }

    private void handleUntrack(long chatId, String url) {
        LinkRequest req = new LinkRequest(url, null, null, null);
        try{
            scrapperClient.removeLink(chatId, req);
            botService.sendMessage(chatId, "Ссылка удалена: " + url);
        } catch (LinkNotFoundException e) {
            botService.sendMessage(chatId, "Ошибка. " + LINK_NOT_FOUND_MESSAGE);
        }
        resetState(chatId);
    }

    private void startUntracking(long chatId) {
        botStates.put(chatId, BotState.WAITING_FOR_UNTRACK_LINK);
        botService.sendMessage(chatId, "Введите ссылку для удаления");
    }

    private void resetState(Long chatId) {
        botStates.remove(chatId);
        sessions.remove(chatId);
    }

    static class SessionData {
        String url;
        LinkType linkType;
        Set<String> tags;
        Map<String, String> filters;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public LinkType getLinkType() {
            return linkType;
        }

        public void setLinkType(LinkType linkType) {
            this.linkType = linkType;
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
