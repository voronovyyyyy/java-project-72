package hexlet.code.dto;

import hexlet.code.model.Url;
import java.sql.Timestamp;

public record UrlSummary(Url url, Integer statusCode, Timestamp checkedAt) {}
