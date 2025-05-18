package backend.academy.scrapper.db_test;

import backend.academy.scrapper.repository.SqlChatRepository;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class SqlChatRepositoryTest extends BaseSqlTest {
    @PostConstruct
    void setUp() {
        chatRepository = new SqlChatRepository(jdbcTemplate);
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("TRUNCATE TABLE chats CASCADE");
    }

    @Test
    void saveNewChatInDbTest() {
        Long chatId = 1L;
        chatRepository.save(chatId);
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
        chatRepository.save(chatId);
        chatRepository.save(chatId);
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
        chatRepository.save(chatId);
        chatRepository.delete(chatId);
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
        chatRepository.save(chatId);
        assertThat(chatRepository.exists(chatId)).isTrue();
        assertThat(chatRepository.exists(999L)).isFalse();
    }

    @Test
    void deleteNonExistingChatTest() {
        assertThatNoException()
            .isThrownBy(() -> chatRepository.delete(999L));
    }
}
