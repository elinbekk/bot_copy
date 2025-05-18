package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UpdateService;
import org.springframework.stereotype.Component;

@Component
public class NotificationSender {
    private final UpdateService updateService;
    private final BotClient botClient;
    private final LinkService linkService;

    public NotificationSender(UpdateService updateService, BotClient botClient, LinkService linkService) {
        this.updateService = updateService;
        this.botClient = botClient;
        this.linkService = linkService;
    }
}
