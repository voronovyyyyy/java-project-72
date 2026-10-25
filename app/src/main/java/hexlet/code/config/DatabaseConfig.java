package hexlet.code.config;

import com.zaxxer.hikari.HikariConfig;
import java.net.URI;

public final class DatabaseConfig {
    private DatabaseConfig() {}

    public static HikariConfig fromUrl(String address) {
        var config = new HikariConfig();
        config.setMaximumPoolSize(5);
        if (address.startsWith("postgres://") || address.startsWith("postgresql://")) {
            var uri = URI.create(address);
            var credentials = uri.getUserInfo().split(":", 2);
            config.setUsername(credentials[0]);
            config.setPassword(credentials.length == 2 ? credentials[1] : "");
            var port = uri.getPort() == -1 ? 5432 : uri.getPort();
            var query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
            config.setJdbcUrl(
                    "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getRawPath() + query);
        } else {
            config.setJdbcUrl(address);
        }
        return config;
    }
}
