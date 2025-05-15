package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class SqlLinkRepository implements LinkRepo {
    private final JdbcTemplate jdbc;
    private final ObjectMapper om;

    public SqlLinkRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.om = objectMapper;
    }

    @Override
    public void save(Long chatId, Link link) {
        String tagsJson;
        String filtersJson;
        try {
            tagsJson = om.writeValueAsString(link.getTags());
            filtersJson = om.writeValueAsString(link.getFilters());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        jdbc.update(
                "INSERT INTO links(chat_id,url,type,last_checked,tags,filters) VALUES(?,?,?,?,?::jsonb,?::jsonb)",
                chatId,
                link.getUrl(),
                link.getLinkType().name(),
                link.getLastCheckedTime(),
                tagsJson,
                filtersJson);
    }

    @Override
    public void delete(Long chatId, String url) {
        jdbc.update("DELETE FROM links WHERE chat_id = ? AND url = ?", chatId, url);
    }

    @Override
    public boolean exists(Long chatId, String url) {
        return false;
    }

    @Override
    public List<Link> findAllLinksByChatId(Long chatId) {
        return List.of();
    }
}
