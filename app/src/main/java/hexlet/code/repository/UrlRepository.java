package hexlet.code.repository;

import hexlet.code.dto.UrlSummary;
import hexlet.code.model.Url;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UrlRepository extends BaseRepository {
    public static void save(Url url) throws SQLException {
        var sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            var createdAt = Timestamp.from(Instant.now());
            statement.setString(1, url.getName());
            statement.setTimestamp(2, createdAt);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return a URL ID");
                }
                url.setId(keys.getLong(1));
                url.setCreatedAt(createdAt);
            }
        }
    }

    public static Optional<Url> find(long id) throws SQLException {
        return findBy("id", id);
    }

    public static Optional<Url> findByName(String name) throws SQLException {
        return findBy("name", name);
    }

    private static Optional<Url> findBy(String column, Object value) throws SQLException {
        // The column is selected only by the two methods above; values are parameters.
        var sql = "SELECT id, name, created_at FROM urls WHERE " + column + " = ?";
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (var result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public static List<UrlSummary> findAllWithLastCheck() throws SQLException {
        var sql =
                """
                SELECT u.id, u.name, u.created_at, c.status_code, c.created_at AS checked_at
                FROM urls u
                LEFT JOIN url_checks c ON c.id = (
                    SELECT id FROM url_checks WHERE url_id = u.id
                    ORDER BY created_at DESC, id DESC LIMIT 1
                )
                ORDER BY u.created_at DESC, u.id DESC
                """;
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql);
                var result = statement.executeQuery()) {
            var urls = new ArrayList<UrlSummary>();
            while (result.next()) {
                urls.add(
                        new UrlSummary(
                                map(result),
                                result.getObject("status_code", Integer.class),
                                result.getTimestamp("checked_at")));
            }
            return urls;
        }
    }

    private static Url map(ResultSet result) throws SQLException {
        var url = new Url(result.getString("name"));
        url.setId(result.getLong("id"));
        url.setCreatedAt(result.getTimestamp("created_at"));
        return url;
    }
}
