package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public void save(Link link) {
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
                link.getChatId(),
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
        Integer cnt = jdbc.queryForObject(
            "SELECT count(1) FROM chats WHERE id = ?", Integer.class, chatId);
        return cnt != null && cnt > 0;
    }

    @Override
    public List<Link> findAllLinksByChatId(Long chatId) {
        return List.of();
    }

    //    @Override
    public Page<Link> findDueLinks(Pageable pg) {
        var sql = """
                SELECT id, chat_id, url, type, last_checked, tags, filters
                FROM links
                WHERE last_checked < NOW()  -- или другой критерий “устаревания”
                ORDER BY last_checked
                LIMIT :limit OFFSET :offset
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("limit", pg.getPageSize());
        params.put("offset", pg.getOffset());

        Object[] args = new Object[]{pg.getPageSize(), pg.getOffset()};
        List<Link> content = jdbc.query(sql, args, this::mapRow);
        return new PageImpl<>(content, pg, content.size());
    }

    private Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            return new Link(
                rs.getLong("id"),
                rs.getString("url"),
                rs.getLong("chat_id"),
                LinkType.valueOf(rs.getString("type")),
                om.readValue(rs.getString("tags"), new TypeReference<Set<String>>() {
                }),
                om.readValue(rs.getString("filters"), new TypeReference<Map<String, String>>() {
                }),
                rs.getTimestamp("last_checked").toInstant().toString()
            );
        } catch (JsonProcessingException e) {
            throw new SQLException("Ошибка десериализации JSONB", e);
        }
    }
}
