package hexlet.code.repository;

import hexlet.code.model.UrlCheck;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class UrlCheckRepository extends BaseRepository {
    public static void save(UrlCheck check) throws SQLException {
        var sql =
                """
                INSERT INTO url_checks (url_id, status_code, title, h1, description, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            var createdAt = Timestamp.from(Instant.now());
            statement.setLong(1, check.getUrlId());
            statement.setInt(2, check.getStatusCode());
            statement.setString(3, check.getTitle());
            statement.setString(4, check.getH1());
            statement.setString(5, check.getDescription());
            statement.setTimestamp(6, createdAt);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return a check ID");
                }
                check.setId(keys.getLong(1));
                check.setCreatedAt(createdAt);
            }
        }
    }

    public static List<UrlCheck> findByUrlId(long urlId) throws SQLException {
        var sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC, id DESC";
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, urlId);
            try (var result = statement.executeQuery()) {
                var checks = new ArrayList<UrlCheck>();
                while (result.next()) {
                    var check =
                            new UrlCheck(
                                    result.getLong("url_id"),
                                    result.getInt("status_code"),
                                    result.getString("title"),
                                    result.getString("h1"),
                                    result.getString("description"));
                    check.setId(result.getLong("id"));
                    check.setCreatedAt(result.getTimestamp("created_at"));
                    checks.add(check);
                }
                return checks;
            }
        }
    }
}
