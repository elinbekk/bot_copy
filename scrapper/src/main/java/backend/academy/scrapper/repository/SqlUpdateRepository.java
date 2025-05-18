package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.UpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import static java.util.stream.Collectors.joining;

@Component
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class SqlUpdateRepository implements UpdateRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper om;

    public SqlUpdateRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbc = jdbcTemplate;
        this.om = objectMapper;
    }

    @Override
    public void save(Long linkId, JsonNode payload, Timestamp occurredAt) {
        try {
            jdbc.update(
                "INSERT INTO updates(link_id, occurred_at, payload) VALUES(?,?,?)",
                linkId,
                occurredAt,
                om.writeValueAsString(payload)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UpdateDto> findUnsents(int page, int size) {
        String sql = """
      SELECT id, link_id, occurred_at, payload, sent
      FROM updates
      WHERE sent = false
      ORDER BY occurred_at
      LIMIT ? OFFSET ?
      """;
        return jdbc.query(sql, new UpdateRowMapper(), size, page * size);
    }

    @Override
    public void markSent(List<Long> updateIds) {
        String inSql = updateIds.stream().map(id -> "?").collect(joining(","));
        jdbc.update(
            "UPDATE updates SET sent = true WHERE id IN (" + inSql + ")",
            updateIds.toArray()
        );
    }

    private static class UpdateRowMapper implements RowMapper<UpdateDto> {
        public UpdateDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            UpdateDto u = new UpdateDto();
            u.setId(rs.getLong("id"));
            u.setLinkId(rs.getLong("link_id"));
            u.setOccurredAt(rs.getTimestamp("occurred_at"));
            u.setSent(rs.getBoolean("sent"));
            try {
                u.setPayload(
                    new ObjectMapper().readTree(rs.getString("payload"))
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return u;
        }
    }
}
