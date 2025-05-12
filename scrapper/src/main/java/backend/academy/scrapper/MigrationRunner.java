package backend.academy.scrapper;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.stereotype.Component;

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
            liquibase.afterPropertiesSet();
        } catch (LiquibaseException e) {
            throw new RuntimeException("Ошибка при миграции БД", e);
        }
    }
}
