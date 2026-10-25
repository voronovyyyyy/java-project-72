package hexlet.code.controller;

import hexlet.code.dto.Flash;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.service.UrlCheckService;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import java.io.IOException;
import java.sql.SQLException;
import kong.unirest.core.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UrlCheckController {
    private static final Logger LOG = LoggerFactory.getLogger(UrlCheckController.class);
    private final UrlCheckService service;

    public UrlCheckController(UrlCheckService service) {
        this.service = service;
    }

    public void create(Context ctx) throws SQLException {
        var url = UrlController.findUrl(ctx);
        try {
            var check = service.analyze(url);
            UrlCheckRepository.save(check);
            ctx.sessionAttribute("flash", new Flash("Страница успешно проверена", false));
        } catch (IOException | UnirestException exception) {
            LOG.warn("Check failed for {}: {}", url.getName(), exception.getMessage());
            ctx.sessionAttribute("flash", new Flash("Произошла ошибка при проверке", true));
        }
        ctx.redirect(NamedRoutes.urlPath(url.getId()));
    }
}
