package backend.academy.bot.helper;

import static backend.academy.bot.constant.BotMessages.FORMAT_LIST_MESSAGE;

import backend.academy.bot.dto.LinkResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class LinkFormatter {
    public String getFormattedLinksList(List<LinkResponse> links) {
        return links.stream().map(this::formatLink).collect(Collectors.joining("\n\n"));
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
                formattedTime);
    }

    private @NotNull String getFormattedTime(LinkResponse link) {
        ZonedDateTime zonedDateTime = Instant.parse(link.getLastCheckedTime()).atZone(ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z").format(zonedDateTime);
    }
}
