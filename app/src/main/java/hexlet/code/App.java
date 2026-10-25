package hexlet.code;

import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.config.DatabaseConfig;
import hexlet.code.controller.RootController;
import hexlet.code.controller.UrlCheckController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.service.UrlCheckService;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.JavalinStaticResourceHandler;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import kong.unirest.core.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private static final Path TEMPLATES_PATH = Path.of("src", "main", "resources", "templates");
    private static final Path STATIC_PATH = Path.of("src", "main", "resources", "static");
    private static final Path JTE_CLASSES_PATH = Path.of("jte-classes");

    private App() {}

    public static Javalin getApp() throws IOException, SQLException {
        var dataSource = createDataSource();
        initializeSchema(dataSource);
        BaseRepository.setDataSource(dataSource);
        var client = Unirest.spawnInstance();
        client.config().connectTimeout(3000).requestTimeout(5000).followRedirects(true);
        var checkController = new UrlCheckController(new UrlCheckService(client));
        var development = isDevelopment();
        LOG.info("Mode: {}", development ? "development (source files)" : "production (classpath)");
        return Javalin.create(
                config -> {
                    config.resourceHandler(new JavalinStaticResourceHandler());
                    if (development) {
                        config.bundledPlugins.enableDevLogging();
                        config.staticFiles.add(STATIC_PATH.toString(), Location.EXTERNAL);
                    } else {
                        config.staticFiles.add("/static", Location.CLASSPATH);
                    }
                    config.fileRenderer(new JavalinJte(createTemplateEngine()));
                    config.routes.get(NamedRoutes.rootPath(), RootController::index);
                    config.routes.get(NamedRoutes.urlsPath(), UrlController::index);
                    config.routes.post(NamedRoutes.urlsPath(), UrlController::create);
                    config.routes.get(NamedRoutes.urlPath("{id}"), UrlController::show);
                    config.routes.post(NamedRoutes.urlChecksPath("{id}"), checkController::create);
                    config.events.serverStopped(
                            () -> {
                                client.close();
                                dataSource.close();
                            });
                });
    }

    private static HikariDataSource createDataSource() {
        var config =
                DatabaseConfig.fromUrl(
                        System.getenv()
                                .getOrDefault(
                                        "JDBC_DATABASE_URL",
                                        "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1"));
        return new HikariDataSource(config);
    }

    private static void initializeSchema(HikariDataSource dataSource)
            throws IOException, SQLException {
        String schema;
        try (var input = App.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (input == null) {
                throw new IOException("schema.sql is missing");
            }
            schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement()) {
            for (var sql : schema.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }

    private static boolean isDevelopment() {
        return "development".equals(System.getenv("APP_ENV")) && Files.isDirectory(TEMPLATES_PATH);
    }

    private static TemplateEngine createTemplateEngine() {
        if (isDevelopment()) {
            return TemplateEngine.create(
                    new DirectoryCodeResolver(TEMPLATES_PATH), JTE_CLASSES_PATH, ContentType.Html);
        }
        var resolver = new ResourceCodeResolver("templates", App.class.getClassLoader());
        return TemplateEngine.create(resolver, ContentType.Html);
    }

    public static void main(String[] args) throws IOException, SQLException {
        var port = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
        getApp().start(port);
    }
}
