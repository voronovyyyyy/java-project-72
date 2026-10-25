package hexlet.code.dto;

import java.util.List;

public record UrlsPage(List<UrlSummary> urls, Flash flash) {}
