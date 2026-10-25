package hexlet.code.dto;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import java.util.List;

public record UrlPage(Url url, List<UrlCheck> checks, Flash flash) {}
