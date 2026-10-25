package hexlet.code.controller;

import hexlet.code.dto.Flash;
import hexlet.code.dto.HomePage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.service.UrlNormalizer;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import java.sql.SQLException;
import java.util.Map;

public final class UrlController {
    public static void index(Context ctx) throws SQLException {
        var page =
                new UrlsPage(
                        UrlRepository.findAllWithLastCheck(),
                        ctx.<Flash>consumeSessionAttribute("flash"));
        ctx.render("urls/index.jte", Map.of("page", page));
    }

    public static void show(Context ctx) throws SQLException {
        var url = findUrl(ctx);
        var page =
                new UrlPage(
                        url,
                        UrlCheckRepository.findByUrlId(url.getId()),
                        ctx.<Flash>consumeSessionAttribute("flash"));
        ctx.render("urls/show.jte", Map.of("page", page));
    }

    public static void create(Context ctx) throws SQLException {
        var input = ctx.formParam("url");
        String name;
        try {
            name = UrlNormalizer.normalize(input);
        } catch (IllegalArgumentException exception) {
            var page =
                    new HomePage(input == null ? "" : input, new Flash("Некорректный URL", true));
            ctx.status(422).render("index.jte", Map.of("page", page));
            return;
        }
        var existing = UrlRepository.findByName(name);
        if (existing.isPresent()) {
            redirectToUrl(ctx, existing.get(), "Страница уже существует");
            return;
        }
        var url = new Url(name);
        try {
            UrlRepository.save(url);
        } catch (SQLException exception) {
            // A concurrent request can insert the same normalized name first.
            if (!"23505".equals(exception.getSQLState())) {
                throw exception;
            }
            var duplicate = UrlRepository.findByName(name).orElseThrow(() -> exception);
            redirectToUrl(ctx, duplicate, "Страница уже существует");
            return;
        }
        redirectToUrl(ctx, url, "Страница успешно добавлена");
    }

    public static Url findUrl(Context ctx) throws SQLException {
        long id;
        try {
            id = Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException exception) {
            throw new NotFoundResponse("Страница не найдена");
        }
        return UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Страница не найдена"));
    }

    private static void redirectToUrl(Context ctx, Url url, String message) {
        ctx.sessionAttribute("flash", new Flash(message, false));
        ctx.redirect(NamedRoutes.urlPath(url.getId()));
    }
}
