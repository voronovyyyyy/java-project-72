package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.util.ViewUtils;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

class ViewUtilsTest {
    @Test
    void formatsDatesAndEmptyValues() {
        assertThat(ViewUtils.formatDate(null)).isEmpty();
        assertThat(ViewUtils.formatDate(Timestamp.valueOf("2024-01-01 12:30:00")))
                .isEqualTo("2024-01-01 12:30");
        assertThat(ViewUtils.truncate(null)).isEmpty();
        assertThat(ViewUtils.truncate("short")).isEqualTo("short");
        assertThat(ViewUtils.truncate("x".repeat(200))).hasSize(200);
    }

    @Test
    void truncatesByUnicodeCharactersWithoutSplittingSurrogatePairs() {
        var emoji = new String(Character.toChars(0x1F600));
        assertThat(ViewUtils.truncate(emoji.repeat(201))).isEqualTo(emoji.repeat(200) + "...");
    }
}
