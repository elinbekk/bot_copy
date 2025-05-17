package backend.academy.scrapper.db_test;

import backend.academy.scrapper.repository.SqlChatRepository;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@JdbcTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class SqlChatRepositoryTest {
    private SqlChatRepository chatRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    void setUp() {
        chatRepo = new SqlChatRepository(jdbcTemplate);
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("TRUNCATE TABLE chats CASCADE");
    }

    @Test
    void saveNewChatInDbTest() {
        Long chatId = 1L;
        chatRepo.save(chatId);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM chats WHERE id = ?",
            Integer.class,
            chatId
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void saveDuplicateChatTest() {
        Long chatId = 1L;
        chatRepo.save(chatId);
        chatRepo.save(chatId);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM chats WHERE id = ?",
            Integer.class,
            chatId
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteExistingChatTest() {
        Long chatId = 1L;
        chatRepo.save(chatId);
        chatRepo.delete(chatId);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM chats WHERE id = ?",
            Integer.class,
            chatId
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    void existsChatTest() {
        Long chatId = 1L;
        chatRepo.save(chatId);
        assertThat(chatRepo.exists(chatId)).isTrue();
        assertThat(chatRepo.exists(999L)).isFalse();

    }

    @Test
    void deleteNonExistingChatTest() {
        assertThatNoException()
            .isThrownBy(() -> chatRepo.delete(999L));
    }
}
