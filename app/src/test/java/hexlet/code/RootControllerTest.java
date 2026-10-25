package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import io.javalin.testtools.JavalinTest;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

class RootControllerTest extends AppTestSupport {
    @Test
    void rootContainsTheRequiredForm() {
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.get("/");
                    assertThat(response.code()).isEqualTo(200);
                    var document = Jsoup.parse(response.body().string());
                    assertThat(document.title()).contains("Анализатор страниц");
                    assertThat(document.select("form[action=/urls][method=post]")).hasSize(1);
                    assertThat(document.select("input[type=text][name=url]")).hasSize(1);
                    assertThat(document.select("input[type=submit]").attr("value"))
                            .isEqualTo("Проверить");
                    assertThat(document.select("link[rel=stylesheet]").attr("href"))
                            .isEqualTo("/css/main.css");
                });
    }

    @Test
    void compiledTailwindStylesAreServed() {
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.get("/css/main.css");
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(response.body().string()).contains(".text-5xl", ".bg-blue-600");
                });
    }
}
