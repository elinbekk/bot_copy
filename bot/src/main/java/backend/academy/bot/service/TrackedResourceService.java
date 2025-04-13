package backend.academy.bot.service;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.entity.TrackedResource;
import backend.academy.bot.repository.TrackedResourceRepository;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import static backend.academy.bot.BotMessages.FORMAT_LIST_MESSAGE;
import static backend.academy.bot.BotMessages.LINK_DUPLICATED_MESSAGE;
import static backend.academy.bot.BotMessages.LINK_INCORRECT_MESSAGE;
import static backend.academy.bot.BotMessages.LIST_EMPTY_MESSAGE;
import static backend.academy.bot.BotMessages.LIST_MESSAGE;

@Component
public class TrackedResourceService {
    private final TrackedResourceRepository linkRepository;
    private final BotService botService;

    public TrackedResourceService(TrackedResourceRepository linkRepository, BotService botService) {
        this.linkRepository = linkRepository;
        this.botService = botService;
    }

    public void saveTrackedResource(long chatId, TrackedResource resource) {
        if (linkRepository.existsByChatIdAndLink(chatId, resource.getLink())) {
            botService.sendMessage(chatId, LINK_DUPLICATED_MESSAGE);
            throw new IllegalArgumentException(LINK_DUPLICATED_MESSAGE);
        }
        linkRepository.addResource(chatId, resource);
    }

    public void showTrackedLinks(long chatId) {
        List<TrackedResource> resources = linkRepository.getResourcesByChatId(chatId);

        if (resources.isEmpty()) {
            botService.sendMessage(chatId, LIST_EMPTY_MESSAGE);
            return;
        }

        String response = resources.stream()
            .map(this::formatResource)
            .collect(Collectors.joining("\n\n"));

        botService.sendMessage(chatId, LIST_MESSAGE + response);
    }

    public String formatResource(TrackedResource resource) {
        String filtersStr = resource.getFilters().entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", "));

        ZonedDateTime zonedDateTime = resource.getLastCheckedTime()
            .atZone(ZoneId.systemDefault());


        String formattedTime = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm:ss z")
            .format(zonedDateTime);

        return String.format(
            FORMAT_LIST_MESSAGE,
            resource.getLink(),
            resource.getTags().isEmpty() ? "нет" : String.join(", ", resource.getTags()),
            resource.getFilters().isEmpty() ? "нет" : filtersStr,
            formattedTime);
    }



    public void processInvalidURL(String url) throws MalformedURLException, URISyntaxException {
        if (!isValidURL(url)) {
            throw new IllegalArgumentException(LINK_INCORRECT_MESSAGE);
        }
    }

    boolean isValidURL(String url) throws MalformedURLException, URISyntaxException {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public LinkType detectResourceType(String url) {
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

    public boolean isResourceAlreadyTracked(long chatId, String link) {
        return linkRepository.existsByChatIdAndLink(chatId, link);
    }

    public void removeResource(long chatId, String link) {
        linkRepository.deleteResource(chatId, link);
    }

}
