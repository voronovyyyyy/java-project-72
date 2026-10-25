package hexlet.code.controller;

import hexlet.code.dto.Flash;
import hexlet.code.dto.HomePage;
import io.javalin.http.Context;
import java.util.Map;

public final class RootController {
    public static void index(Context ctx) {
        ctx.render(
                "index.jte",
                Map.of("page", new HomePage("", ctx.<Flash>consumeSessionAttribute("flash"))));
    }
}
