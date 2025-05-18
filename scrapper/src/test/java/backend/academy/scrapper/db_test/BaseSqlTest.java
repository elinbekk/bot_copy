package backend.academy.scrapper.db_test;

import backend.academy.scrapper.repository.impl.SqlChatRepository;
import backend.academy.scrapper.repository.impl.SqlLinkRepository;
import backend.academy.scrapper.repository.impl.SqlUpdateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

@JdbcTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class BaseSqlTest {
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    protected SqlUpdateRepository updateRepository;
    protected SqlLinkRepository linkRepository;
    protected SqlChatRepository chatRepository;
    protected ObjectMapper objectMapper = new ObjectMapper();
}
