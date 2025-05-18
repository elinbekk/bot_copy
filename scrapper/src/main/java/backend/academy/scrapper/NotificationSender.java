package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UpdateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class NotificationSender {
    private final UpdateService updateService;
    private final BotClient botClient;
    private final LinkService linkService;
    private final int pageSize;


    public NotificationSender(UpdateService updateService, BotClient botClient, LinkService linkService, int pageSize) {
        this.updateService = updateService;
        this.botClient = botClient;
        this.linkService = linkService;
        this.pageSize = pageSize;
    }
}
