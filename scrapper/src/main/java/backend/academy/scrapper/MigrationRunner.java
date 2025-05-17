package backend.academy.scrapper;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
public class MigrationRunner {
    private final DataSource dataSource;

    public MigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void migrate() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:/migrations/changelog-master.xml");
        try {
            Thread.sleep(3000);
            liquibase.afterPropertiesSet();
        } catch (LiquibaseException e) {
            throw new RuntimeException("Ошибка при миграции БД", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
