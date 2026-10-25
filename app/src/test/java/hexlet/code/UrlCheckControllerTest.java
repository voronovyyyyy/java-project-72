package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.testtools.JavalinTest;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlCheckControllerTest extends AppTestSupport {
    private static MockWebServer remote;

    @BeforeAll
    static void startRemote() throws Exception {
        remote = new MockWebServer();
        remote.start();
    }

    @AfterAll
    static void stopRemote() throws Exception {
        remote.close();
    }

    @Test
    void successfulCheckParsesHtmlPersistsAndShowsResults() throws Exception {
        String html;
        try (var input = getClass().getResourceAsStream("/fixtures/success.html")) {
            html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        remote.enqueue(new MockResponse().setResponseCode(200).setBody(html));
        var url = saveRemoteUrl();
        JavalinTest.test(
                app,
                followRedirects(),
                (server, client) -> {
                    var response = client.post(NamedRoutes.urlChecksPath(url.getId()));
                    assertThat(response.code()).isEqualTo(200);
                    var document = Jsoup.parse(response.body().string());
                    assertThat(document.select("[role=alert]").text())
                            .isEqualTo("Страница успешно проверена");
                    assertThat(document.select("table[data-test=checks] tbody tr")).hasSize(1);
                    var check = UrlCheckRepository.findByUrlId(url.getId()).getFirst();
                    assertThat(check.getStatusCode()).isEqualTo(200);
                    assertThat(check.getTitle()).isEqualTo("Awesome page");
                    assertThat(check.getH1())
                            .isEqualTo("Do not expect a miracle, miracles yourself!");
                    assertThat(check.getDescription()).isEqualTo("Statements of great people");
                    assertThat(check.getCreatedAt()).isNotNull();
                    assertThat(document.select("table[data-test=checks]").text())
                            .contains(check.getH1(), check.getTitle(), check.getDescription());
                });
        assertThat(remote.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/");
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404, 500, 503})
    void httpErrorShowsFlashAndDoesNotSaveCheck(int status) throws Exception {
        remote.enqueue(new MockResponse().setResponseCode(status));
        var url = saveRemoteUrl();
        JavalinTest.test(
                app,
                followRedirects(),
                (server, client) -> {
                    var response = client.post(NamedRoutes.urlChecksPath(url.getId()));
                    assertThat(response.code()).isEqualTo(200);
                    assertThat(Jsoup.parse(response.body().string()).select("[role=alert]").text())
                            .isEqualTo("Произошла ошибка при проверке");
                    assertThat(UrlCheckRepository.findByUrlId(url.getId())).isEmpty();
                });
        assertThat(remote.takeRequest(1, TimeUnit.SECONDS)).isNotNull();
    }

    @Test
    void unavailableServerShowsErrorWithoutSavingCheck() throws Exception {
        String name;
        try (var unavailable = new MockWebServer()) {
            unavailable.start();
            name = unavailable.url("/").toString();
        }
        var url = new Url(name);
        UrlRepository.save(url);
        JavalinTest.test(
                app,
                followRedirects(),
                (server, client) -> {
                    var response = client.post(NamedRoutes.urlChecksPath(url.getId()));
                    assertThat(Jsoup.parse(response.body().string()).select("[role=alert]").text())
                            .isEqualTo("Произошла ошибка при проверке");
                    assertThat(UrlCheckRepository.findByUrlId(url.getId())).isEmpty();
                });
    }

    @Test
    void missingSeoTagsAreStoredAsEmptyStrings() throws Exception {
        remote.enqueue(new MockResponse().setBody("<html><body>Empty page</body></html>"));
        var url = saveRemoteUrl();
        JavalinTest.test(
                app,
                (server, client) -> {
                    assertThat(client.post(NamedRoutes.urlChecksPath(url.getId())).code())
                            .isEqualTo(302);
                    var check = UrlCheckRepository.findByUrlId(url.getId()).getFirst();
                    assertThat(check.getTitle()).isEmpty();
                    assertThat(check.getH1()).isEmpty();
                    assertThat(check.getDescription()).isEmpty();
                });
        assertThat(remote.takeRequest(1, TimeUnit.SECONDS)).isNotNull();
    }

    @Test
    void missingUrlCannotBeChecked() {
        JavalinTest.test(
                app,
                (server, client) ->
                        assertThat(client.post(NamedRoutes.urlChecksPath(999999)).code())
                                .isEqualTo(404));
    }

    private Url saveRemoteUrl() throws Exception {
        var url = new Url(remote.url("/").toString());
        UrlRepository.save(url);
        return url;
    }
}
