package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import io.javalin.Javalin;
import io.javalin.testtools.TestConfig;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import org.junit.jupiter.api.BeforeEach;

abstract class AppTestSupport {
    protected Javalin app;

    @BeforeEach
    void setUpApplication() throws Exception {
        var jdbc =
                System.getenv()
                        .getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1");
        assertThat(jdbc).startsWith("jdbc:h2:mem:");
        app = App.getApp();
        try (var connection = DriverManager.getConnection(jdbc);
                var statement = connection.createStatement();
                var input = App.class.getClassLoader().getResourceAsStream("schema.sql")) {
            statement.execute("DROP ALL OBJECTS");
            var schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (var sql : schema.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }

    protected TestConfig followRedirects() {
        return new TestConfig(
                true,
                true,
                HttpClient.newBuilder()
                        .cookieHandler(new CookieManager())
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build());
    }
}
