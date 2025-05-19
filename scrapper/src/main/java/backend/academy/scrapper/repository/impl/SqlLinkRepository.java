package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.repository.LinkRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlLinkRepository implements LinkRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper om;

    public SqlLinkRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.om = objectMapper;
    }

    @Override
    public Long save(Link link) {
        String tagsJson, filtersJson;
        try {
            tagsJson = om.writeValueAsString(link.getTags());
            filtersJson = om.writeValueAsString(link.getFilters());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String sql = """
            INSERT INTO links(
              chat_id, url, type, last_checked, tags, filters
            ) VALUES(?, ?, ?, ?, ?, ?)
            returning id
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, link.getChatId());
            ps.setString(2, link.getUrl());
            ps.setString(3, link.getLinkType().name());
            ps.setTimestamp(4, Timestamp.from(Instant.parse(link.getLastCheckedTime())));
            ps.setString(5, tagsJson);
            ps.setString(6, filtersJson);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            return key.longValue();
        } else {
            throw new IllegalStateException("Failed to retrieve generated key for Link");
        }
    }

    @Override
    public void delete(Long chatId, String url) {
        jdbc.update("DELETE FROM links WHERE chat_id = ? AND url = ?", chatId, url);
    }

    @Override
    public boolean exists(Long chatId, String url) {
        Integer cnt = jdbc.queryForObject(
            "SELECT count(1) FROM links WHERE links.chat_id = ? AND url = ?", Integer.class, chatId, url);
        return cnt != null && cnt > 0;
    }

    @Override
    public List<Link> findAllLinksByChatId(Long chatId) {
        var sql = """
                SELECT id, chat_id, url, type, last_checked, tags, filters
                FROM links
                WHERE chat_id = ?
            """;

        List<Link> linksByChatId = jdbc.query(sql, this::mapRow, chatId);
        return linksByChatId;
    }

    @Override
    public Page<Link> findDueLinks(Pageable pg) {
        String sql = """
                SELECT id, chat_id, url, type, last_checked, tags, filters
                FROM links
                WHERE last_checked < NOW()
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

    @Override
    public void updateLastChecked(Long linkId, Timestamp when) {
        String query = "update links set last_checked = ? where id = ?";
        jdbc.update(query, when, linkId);
    }

    @Override
    public Link findLinkById(Long linkId) {
        String query = "select * from links where id = ?";
        return jdbc.queryForObject(query, this::mapRow, linkId);
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
            throw new SQLException("Ошибка десериализации", e);
        }
    }
}
