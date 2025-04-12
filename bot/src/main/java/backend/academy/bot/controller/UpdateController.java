package backend.academy.bot.controller;


import backend.academy.bot.entity.LinkType;
import backend.academy.bot.entity.LinkUpdate;
import backend.academy.bot.entity.TrackedResource;
import backend.academy.bot.repository.TrackedResourceRepository;
import backend.academy.bot.service.BotService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import static backend.academy.bot.BotMessages.UPDATE_MESSAGE;

@RestController
@RequestMapping("/api")
public class UpdateController {
    private static final Logger log = LoggerFactory.getLogger(UpdateController.class);
    private final BotService botService;
    private final TrackedResourceRepository repository;
    private final Clock clock;

    public UpdateController(BotService botService, TrackedResourceRepository repository, Clock clock) {
        this.botService = botService;
        this.repository = repository;
        this.clock = clock;
    }

    @GetMapping("/links")
    public ResponseEntity<List<TrackedResource>> getLinksToCheck(@RequestParam String interval,
                                                                 @RequestParam(required = false) LinkType linkType) {
        try {
            Duration checkInterval = parseInterval(interval);
            Instant checkFrom = Instant.now(clock).minus(checkInterval);

            List<TrackedResource> result = repository.findByLastCheckedBefore(checkFrom, linkType);
            return ResponseEntity.ok(result);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving links", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/updates")
    public ResponseEntity<?> handleUpdate(@RequestBody LinkUpdate update) {
        try {
            repository.updateLastChecked(update.url(), Instant.now(clock));
            String message = formatUpdateMessage(update);
            botService.sendMessage(update.chatId(), message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка обновления:", e);
            return ResponseEntity.internalServerError().build();
        }
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
            default -> throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Неподдерживаемый интервал: " + interval + ". Допустимые значения: 5m, 1h, 1d"
            );
        };
    }

}
