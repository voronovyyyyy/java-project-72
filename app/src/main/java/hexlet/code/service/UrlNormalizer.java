package hexlet.code.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class UrlNormalizer {
    private UrlNormalizer() {}

    public static String normalize(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("URL is required");
        }
        try {
            var url = new URI(input.trim()).toURL();
            var protocol = url.getProtocol().toLowerCase(Locale.ROOT);
            var host = url.getHost().toLowerCase(Locale.ROOT);
            if ((!protocol.equals("http") && !protocol.equals("https"))
                    || host.isBlank()
                    || url.getUserInfo() != null
                    || url.getPort() < -1
                    || url.getPort() == 0
                    || url.getPort() > 65535) {
                throw new IllegalArgumentException("Invalid HTTP URL");
            }
            return new URI(protocol, null, host, url.getPort(), null, null, null).toASCIIString();
        } catch (MalformedURLException | URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid URL", exception);
        }
    }
}
