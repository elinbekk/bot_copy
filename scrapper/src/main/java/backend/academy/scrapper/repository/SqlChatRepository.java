package backend.academy.scrapper.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class SqlChatRepository implements ChatRepository {
    private final JdbcTemplate jdbc;

    public SqlChatRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Long chatId) {
        jdbc.update("INSERT INTO chats(id) VALUES(?) ON CONFLICT DO NOTHING", chatId);
    }

    @Override
    public void delete(Long chatId) {
        jdbc.update("DELETE FROM chats WHERE id = ?", chatId);
    }

    @Override
    public boolean exists(Long chatId) {
        Integer cnt = jdbc.queryForObject(
            "SELECT count(1) FROM chats WHERE id = ?", Integer.class, chatId);
        return cnt != null && cnt > 0;
    }
}
