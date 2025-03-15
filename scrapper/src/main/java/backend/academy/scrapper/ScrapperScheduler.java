package backend.academy.scrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class ScrapperScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ScrapperScheduler.class);
    @Scheduled(fixedRate = 5000) // Каждые 5 секунд
    public void scrape() {
        logger.info("✅ Scraper is running...");
    }
}
