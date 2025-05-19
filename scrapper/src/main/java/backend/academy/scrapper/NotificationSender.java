package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UpdateService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationSender {
    private static final Logger logger = LoggerFactory.getLogger(NotificationSender.class);
    private final UpdateService updateService;
    private final BotClient botClient;
    private final LinkService linkService;

    public NotificationSender(UpdateService updateService, BotClient botClient, LinkService linkService) {
        this.updateService = updateService;
        this.botClient = botClient;
        this.linkService = linkService;
    }

    @Scheduled(fixedRateString = "${app.scheduler.interval-in-ms}")
    public void sendNotifications() {
        List<UpdateDto> unsents = updateService.getAll();
        logger.info("ОТПРАВИТЬ СКОЛЬКИМ: {}", unsents.size());
        for (UpdateDto u : unsents) {
            Link link = linkService.findById(u.getLinkId());
            final String text = getString(u);
            logger.info("ТЕКСТ УВЕДОМЛЕНИЯ:{}", text);
            LinkUpdate dto = new LinkUpdate(
                link.getUrl(),
                text,
                List.of(link.getChatId())
            );
            botClient.sendUpdateNotification(dto);
        }
    }

    private String getString(UpdateDto updateDto) {
        Link link = linkService.findById(updateDto.getLinkId());
        switch (link.getLinkType()) {
            case GITHUB_REPO, GITHUB_PR, GITHUB_ISSUE -> {
                String title = updateDto.getPayload().get("title").asText();
                String user = updateDto.getPayload().get("user").asText();
                String when = updateDto.getPayload().get("createdAt").asText();
                String preview = updateDto.getPayload().get("preview").asText();
                return String.format("%s\nАвтор: %s\nВремя: %s\n%s…", title, user, when, preview);
            }
            case STACKOVERFLOW -> {
                String question = updateDto.getPayload().get("questionTitle").asText();
                String user = updateDto.getPayload().get("user").asText();
                String created = updateDto.getPayload().get("createdAt").asText();
                String preview = updateDto.getPayload().get("preview").asText();
                return String.format("Вопрос: %s\nОт: %s\nВремя: %s\n%s…", question, user, created, preview);
            }
            default ->
                throw new IllegalArgumentException("Неизвестный тип ссылки");
        }
    }
}

