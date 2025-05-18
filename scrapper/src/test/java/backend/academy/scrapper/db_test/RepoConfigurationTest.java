package backend.academy.scrapper.db_test;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.repository.LinkRepo;
import backend.academy.scrapper.repository.impl.OrmLinkRepository;
import backend.academy.scrapper.repository.impl.SqlLinkRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class RepoConfigurationTest {
    private final String accessType;
    private final LinkRepo repository;

    @Autowired
    public RepoConfigurationTest(ScrapperConfig config, LinkRepo repository) {
        this.accessType = config.accessType();
        this.repository = repository;
    }

    @Test
    public void repositoryTypeTest() {
        if (accessType.equals("ORM")) {
            boolean correctDbType = repository instanceof OrmLinkRepository;
            Assertions.assertTrue(correctDbType);
        } else if (accessType.equals("SQL")) {
            boolean correctDbType = repository instanceof SqlLinkRepository;
            Assertions.assertTrue(correctDbType);
        }
    }
}
