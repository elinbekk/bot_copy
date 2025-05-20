package backend.academy.bot.controller;

import static backend.academy.bot.constant.BotMessages.UPDATE_MESSAGE;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
public class LinkUpdateController {
    private final Logger logger = LoggerFactory.getLogger(LinkUpdateController.class);
    private final BotService botService;

    public LinkUpdateController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody LinkUpdate upd) {
        logger.info("Получено обновление для : {}", upd.getLink());
        String text = formatUpdateMessage(upd);
        logger.debug("Сообщение: {}", text);
        logger.info("Отправка обновления {} чатам", upd.getTgChatIds().size());
        for (Long chatId : upd.getTgChatIds()) {
            botService.sendMessage(chatId, text);
        }
        logger.info("Обновление обработано успешно");
        return ResponseEntity.ok().build();
    }

    private String formatUpdateMessage(LinkUpdate update) {
        return String.format(UPDATE_MESSAGE, update.getLink(), update.getDescription());
    }
}
