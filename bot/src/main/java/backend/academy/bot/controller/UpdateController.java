package backend.academy.bot.controller;


import backend.academy.bot.service.BotService;
import backend.academy.bot.entity.LinkUpdate;
import backend.academy.bot.entity.TrackedResource;
import backend.academy.bot.repository.TrackedResourceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import static backend.academy.bot.BotMessages.UPDATE_MESSAGE;

@RestController
@RequestMapping("/api")
public class UpdateController {
    private final BotService botService;
    private final TrackedResourceRepository repository;

    public UpdateController(BotService botService, TrackedResourceRepository repository) {
        this.botService = botService;
        this.repository = repository;
    }

    @GetMapping("/links")
    public List<TrackedResource> getLinksToCheck(@RequestParam String interval) {
        Duration checkInterval = parseInterval(interval);
        Instant checkFrom = Instant.now().minus(checkInterval);
        return repository.getAllLinks().stream()
            .filter(r -> r.getLastCheckedTime().isBefore(checkFrom))
            .collect(Collectors.toList());
    }

    @PostMapping("/updates")
    public void handleUpdate(@RequestBody LinkUpdate update) {
        repository.updateLastChecked(update.url(), Instant.now());
        String message = formatUpdateMessage(update);
        botService.sendMessage(update.chatId(), message);
    }

    private String formatUpdateMessage(LinkUpdate update) {
        return String.format(
            UPDATE_MESSAGE,
            update.url(),
            update.description(),
            update.url()
        );
    }

    private Duration parseInterval(String interval) {
        return switch (interval.toLowerCase()) {
            case "5m" -> Duration.ofMinutes(5);
            case "1h" -> Duration.ofHours(1);
            case "1d" -> Duration.ofDays(1);
            default -> throw new IllegalArgumentException(
                "Неподдерживаемый интервал: " + interval +
                    ". Допустимые значения: 5m, 1h, 1d"
            );
        };
    }

}
