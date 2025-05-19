package backend.academy.scrapper;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.dto.Link;
import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UpdateService;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LinkCheckerScheduler {
    private static final Logger logger = LoggerFactory.getLogger(LinkCheckerScheduler.class);
    private final GithubClient githubClient;
    private final StackOverflowClient stackoverflowClient;
    private final LinkService linkService;
    private final UpdateService updateService;
    private final int pageSize;

    public LinkCheckerScheduler(
        GithubClient githubClient,
        StackOverflowClient stackoverflowClient,
        LinkService linkService, UpdateService updateService,
        @Value("${app.scheduler.page-size:50}") int pageSize) {
        this.githubClient = githubClient;
        this.stackoverflowClient = stackoverflowClient;
        this.linkService = linkService;
        this.pageSize = pageSize;
        this.updateService = updateService;
    }

    @Scheduled(fixedRateString = "${app.scheduler.interval-in-ms}")
    public void checkAllLinks() {
        int page = 0;
        Page<Link> links;
        try {
            do {
                links = linkService.findDueLinks(PageRequest.of(page, pageSize));
                for (Link link : links) {
                    logger.debug("Проверяемая ссылка: {} (Тип: {})", link.getUrl(), link.getLinkType());
                    processUpdateDetails(link);
                }
                page++;
            } while (links.hasNext());
        } catch (Exception e) {
            logger.error("Ошибка планировщика:", e);
        }
    }


    protected boolean isUpdated(Link resource) {
        return switch (resource.getLinkType()) {
            case GITHUB_REPO, GITHUB_ISSUE, GITHUB_PR -> githubClient.hasUpdates(resource);
            case STACKOVERFLOW -> stackoverflowClient.hasUpdates(resource);
        };
    }

    private void processUpdateDetails(Link link) {
        Optional<GithubClient.Detail> githubDetail;
        Optional<StackOverflowClient.Detail> soDetail;
        switch (link.getLinkType()) {
            case GITHUB_REPO, GITHUB_ISSUE, GITHUB_PR -> {
                githubDetail = githubClient.fetchDetail(link);
                githubDetail.ifPresent(detail ->saveUpdateDetail(link, detail.getLastUpdate(), detail.getPayload()));
            }
            case STACKOVERFLOW -> {
                soDetail = stackoverflowClient.fetchDetail(link);
                soDetail.ifPresent(detail -> saveUpdateDetail(link, detail.getLastUpdate(), detail.getPayload()));
            }
        }
    }

    private void saveUpdateDetail(Link link, Instant lastUpdate, JsonNode payload) {
        UpdateDto upd = new UpdateDto(
            link.getLinkId(),
            Timestamp.from(lastUpdate),
            payload,
            false
        );
        updateService.save(upd);
        linkService.updateLastChecked(link.getLinkId(), Timestamp.from(Instant.now()));
    }
}
