package backend.academy.bot.controller;


import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static backend.academy.bot.BotMessages.UPDATE_MESSAGE;


@RestController
@RequestMapping("/updates")
public class LinkUpdateController {
    private static final Logger log = LoggerFactory.getLogger(LinkUpdateController.class);
    private final BotService botService;

    public LinkUpdateController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody LinkUpdate upd) {
        String text = formatUpdateMessage(upd);
        //todo: need NPE fix
        for (Long chatId : upd.getTgChatIds()) {
            botService.sendMessage(chatId, text);
        }
        return ResponseEntity.ok().build();
    }

    private String formatUpdateMessage(LinkUpdate update) {
        return String.format(
            UPDATE_MESSAGE,
            update.getLink(),
            update.getDescription()
        );
    }
}

