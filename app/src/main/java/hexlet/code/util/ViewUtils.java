package hexlet.code.util;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public final class ViewUtils {
    private static final int MAX_TEXT_LENGTH = 200;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ViewUtils() {}

    public static String truncate(String value) {
        if (value == null) {
            return "";
        }
        var length = value.codePointCount(0, value.length());
        if (length <= MAX_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, value.offsetByCodePoints(0, MAX_TEXT_LENGTH)) + "...";
    }

    public static String formatDate(Timestamp value) {
        return value == null ? "" : value.toLocalDateTime().format(DATE_FORMAT);
    }
}
