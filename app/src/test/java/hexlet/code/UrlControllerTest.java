package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.testtools.JavalinTest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlControllerTest extends AppTestSupport {
    @Test
    void indexContainsUrlsAndTheLatestCheckInNewestFirstOrder() throws Exception {
        var first = new Url("https://first.test");
        UrlRepository.save(first);
        var second = new Url("https://second.test");
        UrlRepository.save(second);
        UrlCheckRepository.save(new UrlCheck(first.getId(), 201, "", "", ""));
        UrlCheckRepository.save(new UrlCheck(first.getId(), 200, "", "", ""));
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.get(NamedRoutes.urlsPath());
                    assertThat(response.code()).isEqualTo(200);
                    var rows =
                            Jsoup.parse(response.body().string())
                                    .select("table[data-test=urls] tbody tr");
                    assertThat(rows).hasSize(2);
                    assertThat(rows.get(0).select("a").text()).isEqualTo(second.getName());
                    assertThat(rows.get(1).select("td").last().text()).isEqualTo("200");
                    assertThat(rows.get(1).text()).doesNotContain("201");
                    assertThat(rows.get(1).select("td").get(3).text()).isNotEmpty();
                });
    }

    @Test
    void emptyIndexStillContainsTheRequiredTable() {
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.get(NamedRoutes.urlsPath());
                    assertThat(response.code()).isEqualTo(200);
                    var document = Jsoup.parse(response.body().string());
                    assertThat(document.select("table[data-test=urls]")).hasSize(1);
                    assertThat(document.select("table[data-test=urls] tbody tr")).isEmpty();
                });
    }

    @Test
    void createNormalizesPersistsAndOpensTheNewUrl() {
        JavalinTest.test(
                app,
                followRedirects(),
                (server, client) -> {
                    var response =
                            client.post(
                                    NamedRoutes.urlsPath(),
                                    form("https://Example.com:8080/path?q=1#part"));
                    assertThat(response.code()).isEqualTo(200);
                    var saved = UrlRepository.findByName("https://example.com:8080").orElseThrow();
                    var document = Jsoup.parse(response.body().string());
                    assertThat(document.select("[role=alert]").text())
                            .isEqualTo("Страница успешно добавлена");
                    assertThat(document.select("table[data-test=url]").text())
                            .contains(saved.getName());
                    assertThat(
                                    document.select("table[data-test=url] tr")
                                            .first()
                                            .select("td")
                                            .last()
                                            .text())
                            .isEqualTo(saved.getId().toString());
                });
    }

    @Test
    void duplicateOpensExistingUrlAndDoesNotInsertAnotherRow() throws Exception {
        var url = new Url("https://example.com");
        UrlRepository.save(url);
        JavalinTest.test(
                app,
                followRedirects(),
                (server, client) -> {
                    var response =
                            client.post(
                                    NamedRoutes.urlsPath(),
                                    form("https://example.com/another-page"));
                    assertThat(response.code()).isEqualTo(200);
                    var document = Jsoup.parse(response.body().string());
                    assertThat(document.select("[role=alert]").text())
                            .isEqualTo("Страница уже существует");
                    assertThat(
                                    document.select("table[data-test=url] tr")
                                            .first()
                                            .select("td")
                                            .last()
                                            .text())
                            .isEqualTo(url.getId().toString());
                    assertThat(UrlRepository.findAllWithLastCheck()).hasSize(1);
                });
    }

    @Test
    void createRespondsWithRedirectToTheUrlResource() {
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.post(NamedRoutes.urlsPath(), form("https://example.com"));
                    var url = UrlRepository.findByName("https://example.com").orElseThrow();
                    assertThat(response.code()).isEqualTo(302);
                    assertThat(response.headers().get("Location"))
                            .containsExactly(NamedRoutes.urlPath(url.getId()));
                });
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "httpsss://abcabca@test.ru",
                "",
                "not a url",
                "ftp://example.com",
                "https://"
            })
    void invalidUrlRendersTheSameFormWith422(String input) {
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.post(NamedRoutes.urlsPath(), form(input));
                    assertThat(response.code()).isEqualTo(422);
                    assertThat(response.headers().get("Location")).isNull();
                    var document = Jsoup.parse(response.body().string());
                    assertThat(document.select("[role=alert]").text())
                            .isEqualTo("Некорректный URL");
                    assertThat(document.select("input[name=url]").val()).isEqualTo(input);
                    assertThat(UrlRepository.findAllWithLastCheck()).isEmpty();
                });
    }

    @Test
    void missingUrlFieldIsInvalid() {
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.post(NamedRoutes.urlsPath(), "");
                    assertThat(response.code()).isEqualTo(422);
                    assertThat(response.body().string()).contains("Некорректный URL");
                });
    }

    @Test
    void showContainsUrlDetailsCheckFormAndEmptyChecks() throws Exception {
        var url = new Url("https://example.com");
        UrlRepository.save(url);
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.get(NamedRoutes.urlPath(url.getId()));
                    assertThat(response.code()).isEqualTo(200);
                    var document = Jsoup.parse(response.body().string());
                    assertThat(document.select("table[data-test=url]").text())
                            .contains(url.getName());
                    assertThat(document.select("form[method=post]").attr("action"))
                            .isEqualTo(NamedRoutes.urlChecksPath(url.getId()));
                    assertThat(document.select("input[type=submit]").val())
                            .isEqualTo("Запустить проверку");
                    assertThat(document.select("table[data-test=checks] tbody tr")).isEmpty();
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"999999", "invalid", "-1"})
    void missingUrlReturns404(String id) {
        JavalinTest.test(
                app,
                (server, client) ->
                        assertThat(client.get(NamedRoutes.urlPath(id)).code()).isEqualTo(404));
    }

    @Test
    void showTruncatesAllSeoFieldsButPreservesTheirFullDatabaseValues() throws Exception {
        var url = new Url("https://example.com");
        UrlRepository.save(url);
        var value = "я".repeat(250);
        UrlCheckRepository.save(new UrlCheck(url.getId(), 200, value, value, value));
        JavalinTest.test(
                app,
                (server, client) -> {
                    var response = client.get(NamedRoutes.urlPath(url.getId()));
                    var cells =
                            Jsoup.parse(response.body().string())
                                    .select("table[data-test=checks] tbody tr td");
                    for (var index : new int[] {2, 3, 4}) {
                        assertThat(cells.get(index).text()).isEqualTo("я".repeat(200) + "...");
                    }
                    assertThat(
                                    UrlCheckRepository.findByUrlId(url.getId())
                                            .getFirst()
                                            .getDescription())
                            .isEqualTo(value);
                });
    }

    @Test
    void showEscapesRemoteHtml() throws Exception {
        var url = new Url("https://example.com");
        UrlRepository.save(url);
        UrlCheckRepository.save(
                new UrlCheck(url.getId(), 200, "<script>alert(1)</script>", "", ""));
        JavalinTest.test(
                app,
                (server, client) -> {
                    var html = client.get(NamedRoutes.urlPath(url.getId())).body().string();
                    assertThat(Jsoup.parse(html).select("script")).isEmpty();
                    assertThat(html).contains("&lt;script&gt;");
                });
    }

    private static String form(String value) {
        return "url=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
