package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import hexlet.code.service.UrlNormalizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UrlNormalizerTest {
    @ParameterizedTest
    @CsvSource({
        "https://some-domain.org/example/path,https://some-domain.org",
        "https://some-domain.org:8080/example/path,https://some-domain.org:8080",
        "HTTP://Example.COM:80/path?q=1#part,http://example.com:80",
        "http://localhost:1234/,http://localhost:1234",
        "http://[::1]:8080/test,http://[::1]:8080"
    })
    void normalizesOrigin(String input, String expected) {
        assertThat(UrlNormalizer.normalize(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                " ",
                "httpsss://abcabca@test.ru",
                "ftp://example.com",
                "https://",
                "https://example.com:0",
                "https://example.com:65536",
                "https://example.com:-2",
                "https://user@example.com",
                "https://invalid host.com"
            })
    void rejectsInvalidUrls(String input) {
        assertThatIllegalArgumentException().isThrownBy(() -> UrlNormalizer.normalize(input));
    }
}
