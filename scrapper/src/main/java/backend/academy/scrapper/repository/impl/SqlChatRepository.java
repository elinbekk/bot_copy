package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.repository.ChatRepository;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
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
    public List<Long> findAllChatIds() {
        String sql = "SELECT id FROM chats";
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("id"));
    }

    @Override
    public boolean exists(Long chatId) {
        Integer cnt = jdbc.queryForObject("SELECT count(1) FROM chats WHERE id = ?", Integer.class, chatId);
        return cnt != null && cnt > 0;
    }
}
