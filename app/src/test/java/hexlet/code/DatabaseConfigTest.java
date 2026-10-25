package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.config.DatabaseConfig;
import org.junit.jupiter.api.Test;

class DatabaseConfigTest {
    @Test
    void acceptsJdbcUrlWithoutChangingIt() {
        var address = "jdbc:postgresql://db:5432/postgres?password=password&user=postgres";
        var config = DatabaseConfig.fromUrl(address);
        assertThat(config.getJdbcUrl()).isEqualTo(address);
        assertThat(config.getMaximumPoolSize()).isEqualTo(5);
    }

    @Test
    void acceptsLocalH2() {
        assertThat(DatabaseConfig.fromUrl("jdbc:h2:mem:project").getJdbcUrl())
                .isEqualTo("jdbc:h2:mem:project");
    }

    @Test
    void convertsRenderConnectionString() {
        var config =
                DatabaseConfig.fromUrl(
                        "postgresql://user:pass%40word@host/database?sslmode=require");
        assertThat(config.getJdbcUrl())
                .isEqualTo("jdbc:postgresql://host:5432/database?sslmode=require");
        assertThat(config.getUsername()).isEqualTo("user");
        assertThat(config.getPassword()).isEqualTo("pass@word");
    }

    @Test
    void acceptsExplicitPortAndEmptyPassword() {
        var config = DatabaseConfig.fromUrl("postgres://user@host:5433/database");
        assertThat(config.getJdbcUrl()).isEqualTo("jdbc:postgresql://host:5433/database");
        assertThat(config.getUsername()).isEqualTo("user");
        assertThat(config.getPassword()).isEmpty();
    }
}
