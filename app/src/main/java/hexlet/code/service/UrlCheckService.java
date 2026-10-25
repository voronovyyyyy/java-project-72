package hexlet.code.service;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import java.io.IOException;
import kong.unirest.core.UnirestInstance;
import org.jsoup.Jsoup;

public final class UrlCheckService {
    private final UnirestInstance client;

    public UrlCheckService(UnirestInstance client) {
        this.client = client;
    }

    public UrlCheck analyze(Url url) throws IOException {
        var response = client.get(url.getName()).asString();
        if (response.getStatus() >= 400) {
            throw new IOException("Remote server returned HTTP " + response.getStatus());
        }
        var document = Jsoup.parse(response.getBody());
        var heading = document.selectFirst("h1");
        var description = document.selectFirst("meta[name=description]");
        return new UrlCheck(
                url.getId(),
                response.getStatus(),
                document.title(),
                heading == null ? "" : heading.text(),
                description == null ? "" : description.attr("content"));
    }
}
