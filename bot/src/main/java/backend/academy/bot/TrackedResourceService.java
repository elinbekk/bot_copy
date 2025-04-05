package backend.academy.bot;

import backend.academy.bot.entity.LinkType;
import backend.academy.bot.entity.TrackedResource;
import backend.academy.bot.repository.LinkRepository;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static backend.academy.bot.BotMessages.LINK_DUPLICATED_MESSAGE;
import static backend.academy.bot.BotMessages.LINK_INCORRECT_MESSAGE;
import static backend.academy.bot.BotMessages.LIST_EMPTY_MESSAGE;
import static backend.academy.bot.BotMessages.LIST_MESSAGE;
import static backend.academy.bot.BotMessages.START_MESSAGE;
import static backend.academy.bot.BotMessages.WAITING_FOR_LINK_MESSAGE;

@Service
public class TrackedResourceService {
    private final LinkRepository linkRepository;
    private final BotService botService;

    public TrackedResourceService(LinkRepository linkRepository, BotService botService) {
        this.linkRepository = linkRepository;
        this.botService = botService;
    }

    public void saveTrackedResource(long chatId, TrackedResource resource) {
        if (linkRepository.existsByChatIdAndLink(chatId, resource.getLink())) {
            botService.sendMessage(chatId, LINK_DUPLICATED_MESSAGE);
            throw new IllegalArgumentException();
        }
        linkRepository.addLink(chatId, resource);
    }

    protected void showTrackedLinks(long chatId) {
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

    private void validateUrl(String url) {
        if (!url.startsWith("http")) {
            throw new IllegalArgumentException(LINK_INCORRECT_MESSAGE);
        }
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

}
